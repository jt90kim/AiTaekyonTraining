// themes.jsx — Two tactical training-app theme directions, each with light + dark.
//
// SIGNAL  → neon-green console HUD. JetBrains Mono everywhere. Hairline rules.
// EMBERS  → amber smartwatch-sport. Space Grotesk display + Geist Mono numerals.
//          Rounded chunky touch targets, deep warm black.
//
// Each theme exposes the same token shape so screens can stay theme-agnostic.

const THEMES = {
  // ─────────────────────────────────────────────────────────────
  // SIGNAL — variation A
  // ─────────────────────────────────────────────────────────────
  signal: {
    name: 'SIGNAL',
    codename: 'A',
    dark: {
      bg:        '#050606',
      surface:   '#0c0e0e',
      surface2:  '#141818',
      line:      'rgba(255,255,255,0.10)',
      lineStrong:'rgba(255,255,255,0.22)',
      grid:      'rgba(255,255,255,0.04)',
      fg:        '#e8efea',
      mute:      'rgba(232,239,234,0.55)',
      mute2:     'rgba(232,239,234,0.32)',
      accent:    '#aaff3c',         // toxic neon green
      accentDim: 'rgba(170,255,60,0.18)',
      accentInk: '#0a1100',
      warn:      '#ff7a3a',
      danger:    '#ff4d4d',
    },
    light: {
      bg:        '#eef0ec',
      surface:   '#ffffff',
      surface2:  '#f5f6f3',
      line:      'rgba(0,0,0,0.10)',
      lineStrong:'rgba(0,0,0,0.30)',
      grid:      'rgba(0,0,0,0.05)',
      fg:        '#0a0c0a',
      mute:      'rgba(10,12,10,0.60)',
      mute2:     'rgba(10,12,10,0.35)',
      accent:    '#3aa30a',
      accentDim: 'rgba(58,163,10,0.15)',
      accentInk: '#ffffff',
      warn:      '#c7521b',
      danger:    '#c41f1f',
    },
    fonts: {
      mono: '"JetBrains Mono", ui-monospace, "SF Mono", Menlo, monospace',
      sans: '"Inter", -apple-system, system-ui, sans-serif',
      kr:   '"Noto Sans KR", "Inter", system-ui, sans-serif',
      display: '"JetBrains Mono", ui-monospace, monospace',
    },
    radius: { sm: 2, md: 4, lg: 6, pill: 999 },
    // Treatment knobs that change personality
    style: {
      cornerStyle: 'sharp',     // chamfer corners
      gridPattern: true,        // background measurement grid
      monoLabels: true,         // ALL CAPS · LETTERSPACED
      brandWord: 'TAEKYON',
      brandSub: 'REACTION TRAINER',
      buildTag: 'BUILD · 0.4.1 · ANDROID',
    },
  },

  // ─────────────────────────────────────────────────────────────
  // EMBERS — variation B
  // ─────────────────────────────────────────────────────────────
  embers: {
    name: 'EMBERS',
    codename: 'B',
    dark: {
      bg:        '#100c08',
      surface:   '#1a140e',
      surface2:  '#241b12',
      line:      'rgba(255,220,170,0.10)',
      lineStrong:'rgba(255,220,170,0.25)',
      grid:      'rgba(255,220,170,0.04)',
      fg:        '#f6ead6',
      mute:      'rgba(246,234,214,0.55)',
      mute2:     'rgba(246,234,214,0.32)',
      accent:    '#ffaa2b',          // amber
      accentDim: 'rgba(255,170,43,0.16)',
      accentInk: '#1a0e00',
      warn:      '#ff6a3a',
      danger:    '#ff3a4d',
    },
    light: {
      bg:        '#f6f1ea',
      surface:   '#ffffff',
      surface2:  '#efe8dc',
      line:      'rgba(60,40,10,0.12)',
      lineStrong:'rgba(60,40,10,0.32)',
      grid:      'rgba(60,40,10,0.05)',
      fg:        '#1a120a',
      mute:      'rgba(26,18,10,0.60)',
      mute2:     'rgba(26,18,10,0.35)',
      accent:    '#c66a00',
      accentDim: 'rgba(198,106,0,0.15)',
      accentInk: '#ffffff',
      warn:      '#9c4413',
      danger:    '#a01a1a',
    },
    fonts: {
      mono: '"Geist Mono", "JetBrains Mono", ui-monospace, monospace',
      sans: '"Space Grotesk", "Inter", system-ui, sans-serif',
      kr:   '"Noto Sans KR", "Space Grotesk", system-ui, sans-serif',
      display: '"Space Grotesk", "Inter", system-ui, sans-serif',
    },
    radius: { sm: 6, md: 12, lg: 18, pill: 999 },
    style: {
      cornerStyle: 'round',
      gridPattern: false,
      monoLabels: false,
      brandWord: 'TAEKYON',
      brandSub: 'Trainer',
      buildTag: 'v0.4.1',
    },
  },
};

// Resolve a theme variant + mode → flat color object + meta.
function resolveTheme(variantId, mode) {
  const v = THEMES[variantId];
  return {
    id: variantId,
    name: v.name,
    codename: v.codename,
    mode,
    c: mode === 'light' ? v.light : v.dark,
    fonts: v.fonts,
    radius: v.radius,
    style: v.style,
  };
}

// Techniques catalog — what the user can pick. Each technique has zero or
// more height variants; the CSV sent to Unity is the list of enabled variant
// IDs joined by comma (e.g. "roundhouse_low,roundhouse_high"). Techniques
// with no captured clips are shown but unselectable so the roadmap is visible.
const TECHNIQUES = [
  {
    id: 'roundhouse',
    name: 'Roundhouse',
    hangul: '후려차기 / 돌려차기',
    romaja: 'Huryeo-chagi / Dollyeo-chagi',
    desc: 'Circular sweep with the shin · whole-body rotation',
    status: 'ready',
    heights: [
      { id: 'roundhouse_low',  label: 'Low',  variants: 4, status: 'ready' },
      { id: 'roundhouse_high', label: 'High', variants: 4, status: 'soon' },
    ],
  },
  {
    id: 'splint',
    name: 'Splint Kick',
    hangul: '내차기',
    romaja: 'Nae-chagi',
    desc: 'Inward sweeping arc — shin travels across the centerline',
    status: 'ready',
    heights: [
      { id: 'splint_low',  label: 'Low',  hangul: '내차기',              variants: 4, status: 'ready' },
      { id: 'splint_high', label: 'High', hangul: '곁차기 / 높은내차기', variants: 4, status: 'ready' },
    ],
  },
  {
    id: 'front',
    name: 'Front Kick',
    hangul: '앞차기',
    romaja: 'Ap-chagi',
    desc: 'Straight thrust off the lead leg',
    status: 'soon',
    heights: [
      { id: 'front_low',  label: 'Low',  variants: 4, status: 'soon' },
      { id: 'front_high', label: 'High', variants: 4, status: 'soon' },
    ],
  },
  {
    id: 'side',
    name: 'Side Kick',
    hangul: '옆차기',
    romaja: 'Yeop-chagi',
    desc: 'Linear push from the hip · long range',
    status: 'soon',
    heights: [
      { id: 'side_low',  label: 'Low',  variants: 4, status: 'soon' },
      { id: 'side_high', label: 'High', variants: 4, status: 'soon' },
    ],
  },
];


// Duration presets — short reaction drills (per requirements: 30s – 5m).
const DURATION_PRESETS = [
  { label: '0:30', seconds: 30 },
  { label: '1:00', seconds: 60 },
  { label: '2:00', seconds: 120 },
  { label: '3:00', seconds: 180 },
  { label: '5:00', seconds: 300 },
];

Object.assign(window, { THEMES, resolveTheme, TECHNIQUES, DURATION_PRESETS });
