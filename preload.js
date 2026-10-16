const { contextBridge } = require('electron');

// Expose a minimal API to the renderer
contextBridge.exposeInMainWorld('mahaam', {
  platform: process.platform,
  version: '1.0.0',
});
