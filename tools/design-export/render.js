// Renders Design-canvas .dc.html artboards into static HTML + PNG (2x).
// usage: node render.js <srcDir> <outDir> <fontPath> <name=Out,name=Out,...>
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const [srcDir, outDir, fontPath, mapArg] = process.argv.slice(2);
const map = Object.fromEntries(mapArg.split(',').map((p) => p.split('=')));

const STUB = `
<script>
class DCLogic { constructor(){ this.props = {}; this.state = {}; } setState(s){ Object.assign(this.state, s); } forceUpdate(){} }
window.DCLogic = DCLogic;
document.addEventListener('DOMContentLoaded', () => {
  const xdc = document.querySelector('x-dc');
  const helmet = xdc.querySelector('helmet');
  if (helmet) { for (const n of [...helmet.childNodes]) document.head.appendChild(n); helmet.remove(); }
  const sc = document.querySelector('script[data-dc-script]');
  const Comp = new Function('DCLogic', sc.textContent + '\\n;return Component;')(DCLogic);
  const c = new Comp(); c.props = c.props || {}; c.state = c.state || {};
  if (c.componentDidMount) { try { c.componentDidMount(); } catch (e) {} }
  const vals = c.renderVals() || {};
  const lookup = (p, s) => { p = p.trim(); if (p === 'true') return true; if (p === 'false') return false; if (/^-?\\d/.test(p)) return Number(p);
    return p.split('.').reduce((o, k) => (o == null ? undefined : o[k]), s); };
  const interp = (str, s) => { const w = str.match(/^\\s*\\{\\{([^}]+)\\}\\}\\s*$/); if (w) return lookup(w[1], s);
    return str.replace(/\\{\\{([^}]+)\\}\\}/g, (_, p) => { const v = lookup(p, s); return v == null ? '' : String(v); }); };
  function proc(node, s) {
    if (node.nodeType === 3) { if (node.textContent.includes('{{')) { const v = interp(node.textContent, s); node.textContent = v == null ? '' : String(v); } return; }
    if (node.nodeType !== 1) return;
    const tag = node.tagName.toLowerCase();
    if (tag === 'sc-for') {
      const list = interp(node.getAttribute('list'), s) || []; const as = node.getAttribute('as') || 'item';
      const tpl = [...node.childNodes];
      list.forEach((item, i) => { for (const t of tpl) { const cl = t.cloneNode(true); node.parentNode.insertBefore(cl, node); proc(cl, Object.assign({}, s, { [as]: item, $index: i })); } });
      node.remove(); return;
    }
    if (tag === 'sc-if') {
      const v = interp(node.getAttribute('value'), s);
      if (v) { for (const ch of [...node.childNodes]) { node.parentNode.insertBefore(ch, node); proc(ch, s); } }
      node.remove(); return;
    }
    for (const a of [...node.attributes]) {
      if (a.value.includes('{{')) { if (/^on[A-Z]/.test(a.name) || /^on/.test(a.name)) { node.removeAttribute(a.name); continue; }
        const v = interp(a.value, s); node.setAttribute(a.name, v == null ? '' : String(v)); }
    }
    for (const ch of [...node.childNodes]) proc(ch, s);
  }
  for (const ch of [...xdc.childNodes]) proc(ch, vals);
  for (const ch of [...xdc.childNodes]) xdc.parentNode.insertBefore(ch, xdc);
  xdc.remove(); sc.remove();
  document.body.setAttribute('data-rendered', '1');
});
</script>`;

(async () => {
  fs.mkdirSync(outDir, { recursive: true });
  const browser = await chromium.launch();
  const ctx = await browser.newContext({ deviceScaleFactor: 2, viewport: { width: 1400, height: 1100 } });
  await ctx.route('https://fonts.googleapis.com/**', (r) => r.fulfill({ contentType: 'text/css',
    body: "@font-face{font-family:'Jost';src:url('https://local.fonts/Jost.ttf') format('truetype');font-weight:100 900;}" }));
  await ctx.route('https://local.fonts/**', (r) => r.fulfill({ contentType: 'font/ttf', body: fs.readFileSync(fontPath) }));
  await ctx.route('https://fonts.gstatic.com/**', (r) => r.abort());
  for (const [src, out] of Object.entries(map)) {
    const raw = fs.readFileSync(path.join(srcDir, src + '.dc.html'), 'utf8');
    const html = raw.replace('<script src="./support.js"></script>', STUB);
    const page = await ctx.newPage();
    await page.setContent(html, { waitUntil: 'load' });
    await page.waitForSelector('body[data-rendered]');
    await page.evaluate(() => document.fonts.ready);
    // freeze animations at a representative frame
    const animated = /@keyframes/.test(raw);
    if (animated) await page.waitForTimeout(Number(process.env.ANIM_WAIT_MS || 1800));
    const root = await page.$('body > div');
    await root.screenshot({ path: path.join(outDir, out + '.png'), animations: animated ? 'allow' : 'disabled' });
    let staticHtml = await page.evaluate(() => {
      document.querySelectorAll('script').forEach((s) => s.remove());
      return '<!doctype html>\n' + document.documentElement.outerHTML;
    });
    staticHtml = staticHtml.replace(/<style>@font-face\{font-family:'Jost';src:url\('https:\/\/local\.fonts[^<]*<\/style>/, '');
    fs.writeFileSync(path.join(outDir, out + '.html'), staticHtml);
    await page.close();
    console.log('ok', src, '->', out);
  }
  await browser.close();
})().catch((e) => { console.error(e); process.exit(1); });
