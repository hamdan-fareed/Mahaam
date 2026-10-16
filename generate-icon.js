const { app, BrowserWindow } = require('electron');
const path = require('path');
const fs = require('fs');
const { execSync } = require('child_process');

app.whenReady().then(async () => {
  const win = new BrowserWindow({
    width: 1024,
    height: 1024,
    show: false,
    webPreferences: {
      offscreen: true,
      nodeIntegration: true,
      contextIsolation: false,
    },
  });

  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
          width: 1024px;
          height: 1024px;
          background: transparent;
          display: flex;
          align-items: center;
          justify-content: center;
          overflow: hidden;
        }
        canvas {
          display: block;
        }
      </style>
    </head>
    <body>
      <canvas id="c" width="1024" height="1024"></canvas>
      <script>
        const canvas = document.getElementById('c');
        const ctx = canvas.getContext('2d');

        // Draw macOS Squircle
        // Standard macOS icon spec: 824x824 squircle centered in 1024x1024
        const size = 824;
        const x = (1024 - size) / 2;
        const y = (1024 - size) / 2;
        const radius = 185;

        function squircle(ctx, x, y, w, h, r) {
          ctx.beginPath();
          ctx.moveTo(x + r, y);
          ctx.lineTo(x + w - r, y);
          ctx.quadraticCurveTo(x + w, y, x + w, y + r);
          ctx.lineTo(x + w, y + h - r);
          ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
          ctx.lineTo(x + r, y + h);
          ctx.quadraticCurveTo(x, y + h, x, y + h - r);
          ctx.lineTo(x, y + r);
          ctx.quadraticCurveTo(x, y, x + r, y);
          ctx.closePath();
        }

        // Soft drop shadow
        ctx.save();
        ctx.shadowColor = 'rgba(0, 0, 0, 0.35)';
        ctx.shadowBlur = 35;
        ctx.shadowOffsetY = 18;
        squircle(ctx, x, y, size, size, radius);
        ctx.fillStyle = '#161923';
        ctx.fill();
        ctx.restore();

        // Icon base clip
        ctx.save();
        squircle(ctx, x, y, size, size, radius);
        ctx.clip();

        // Background: Deep editorial Indigo-Ink with subtle lighting
        const bgGrad = ctx.createLinearGradient(x, y, x + size, y + size);
        bgGrad.addColorStop(0, '#1E2333');
        bgGrad.addColorStop(0.5, '#161924');
        bgGrad.addColorStop(1, '#0E1118');
        ctx.fillStyle = bgGrad;
        ctx.fillRect(x, y, size, size);

        // Subtle 4-Quadrant Matrix geometric background lines (editorial grid)
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.04)';
        ctx.lineWidth = 1.5;
        const cx = 512;
        const cy = 512;

        // Matrix crosshair lines
        ctx.beginPath();
        ctx.moveTo(cx, y + 60);
        ctx.lineTo(cx, y + size - 60);
        ctx.moveTo(x + 60, cy);
        ctx.lineTo(x + size - 60, cy);
        ctx.stroke();

        // Subtle quadrant boxes
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.025)';
        ctx.strokeRect(x + 120, y + 120, cx - (x + 120) - 20, cy - (y + 120) - 20);
        ctx.strokeRect(cx + 20, y + 120, cx - (x + 120) - 20, cy - (y + 120) - 20);
        ctx.strokeRect(x + 120, cy + 20, cx - (x + 120) - 20, cy - (y + 120) - 20);
        ctx.strokeRect(cx + 20, cy + 20, cx - (x + 120) - 20, cy - (y + 120) - 20);

        // Circular focal halo
        const halo = ctx.createRadialGradient(cx, cy - 20, 20, cx, cy - 20, 320);
        halo.addColorStop(0, 'rgba(74, 88, 153, 0.18)');
        halo.addColorStop(0.6, 'rgba(74, 88, 153, 0.05)');
        halo.addColorStop(1, 'rgba(0, 0, 0, 0)');
        ctx.fillStyle = halo;
        ctx.fillRect(x, y, size, size);

        // Thin golden inner border
        ctx.strokeStyle = 'rgba(215, 185, 135, 0.28)';
        ctx.lineWidth = 2;
        squircle(ctx, x + 8, y + 8, size - 16, size - 16, radius - 6);
        ctx.stroke();

        // Arabic Calligraphy: "مهام"
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        
        // Shadow for text depth
        ctx.shadowColor = 'rgba(0, 0, 0, 0.6)';
        ctx.shadowBlur = 18;
        ctx.shadowOffsetY = 6;

        // Primary word "مهام" in elegant Arabic font
        ctx.font = "normal 230px 'DecoType Naskh', 'Geeza Pro', 'Al Nile', 'Baghdad', 'Traditional Arabic', serif";
        
        // Warm ivory-gold gradient for calligraphy
        const textGrad = ctx.createLinearGradient(cx - 150, cy - 80, cx + 150, cy + 40);
        textGrad.addColorStop(0, '#FFFDF8');
        textGrad.addColorStop(0.45, '#F5EDE0');
        textGrad.addColorStop(1, '#E6D3B3');
        ctx.fillStyle = textGrad;
        ctx.fillText('مهام', cx, cy - 35);

        ctx.shadowColor = 'transparent';

        // Secondary English subtitle "MAHAAM" in clean tracked serif
        ctx.font = "300 34px -apple-system, Georgia, 'Times New Roman', serif";
        ctx.fillStyle = 'rgba(215, 185, 135, 0.75)';
        ctx.letterSpacing = '0.35em';
        ctx.fillText('MAHAAM', cx + 6, cy + 150);

        // Subtle chapter numbering in Arabic numerals: "١.٠"
        ctx.font = "normal 24px 'Geeza Pro', serif";
        ctx.fillStyle = 'rgba(255, 255, 255, 0.25)';
        ctx.fillText('نظام المهام', cx, cy + 195);

        // Traditional Red Seal Stamp in top right corner (خاتم عربي)
        const sealX = x + size - 160;
        const sealY = y + 150;
        const sealSize = 52;
        
        ctx.save();
        ctx.fillStyle = 'rgba(166, 61, 64, 0.85)'; // Crimson Ink
        ctx.strokeStyle = 'rgba(215, 185, 135, 0.4)';
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.arc(sealX, sealY, sealSize / 2, 0, Math.PI * 2);
        ctx.fill();
        ctx.stroke();

        ctx.font = "bold 20px 'Geeza Pro', serif";
        ctx.fillStyle = '#FFFDF8';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText('عمل', sealX, sealY - 1);
        ctx.restore();

        ctx.restore(); // end clip
      </script>
    </body>
    </html>
  `;

  await win.loadURL(`data:text/html;charset=utf-8,${encodeURIComponent(html)}`);

  // Wait for rendering
  await new Promise(r => setTimeout(r, 600));

  const image = await win.capturePage({ x: 0, y: 0, width: 1024, height: 1024 });
  const pngBuffer = image.toPNG();

  // Create build folder
  const buildDir = path.join(__dirname, 'build');
  if (!fs.existsSync(buildDir)) fs.mkdirSync(buildDir, { recursive: true });

  const iconPngPath = path.join(buildDir, 'icon.png');
  fs.writeFileSync(iconPngPath, pngBuffer);
  console.log('Saved 1024x1024 PNG to:', iconPngPath);

  // Generate iconset for iconutil
  const iconsetDir = path.join(buildDir, 'icon.iconset');
  if (fs.existsSync(iconsetDir)) fs.rmSync(iconsetDir, { recursive: true, force: true });
  fs.mkdirSync(iconsetDir, { recursive: true });

  const sizes = [
    { name: 'icon_16x16.png', size: 16 },
    { name: 'icon_16x16@2x.png', size: 32 },
    { name: 'icon_32x32.png', size: 32 },
    { name: 'icon_32x32@2x.png', size: 64 },
    { name: 'icon_128x128.png', size: 128 },
    { name: 'icon_128x128@2x.png', size: 256 },
    { name: 'icon_256x256.png', size: 256 },
    { name: 'icon_256x256@2x.png', size: 512 },
    { name: 'icon_512x512.png', size: 512 },
    { name: 'icon_512x512@2x.png', size: 1024 },
  ];

  for (const s of sizes) {
    const dest = path.join(iconsetDir, s.name);
    execSync(`sips -z ${s.size} ${s.size} "${iconPngPath}" --out "${dest}"`, { stdio: 'ignore' });
  }

  const icnsPath = path.join(buildDir, 'icon.icns');
  execSync(`iconutil -c icns "${iconsetDir}" -o "${icnsPath}"`);
  console.log('Generated native macOS ICNS to:', icnsPath);

  app.quit();
});
