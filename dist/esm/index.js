import { registerPlugin } from '@capacitor/core';
const NrfMesh = registerPlugin('NrfMesh', {
    web: () => import('./web').then(m => new m.NrfMeshWeb()),
});
export * from './definitions';
export { NrfMesh };
//# sourceMappingURL=index.js.map