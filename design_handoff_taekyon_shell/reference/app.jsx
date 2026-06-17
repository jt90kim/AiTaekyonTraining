// app.jsx — EMBERS direction, full clickable Android shell flow.
// Three screens (Splash · Setup · Training) plus a tokens sheet for handoff.
// Tweaks: dark/light mode.

const { useState: useStateApp, useEffect: useEffectApp } = React;

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "mode": "dark"
}/*EDITMODE-END*/;

// Default selection — only roundhouse_low has captured clips right now.
const DEFAULT_ENABLED = ['roundhouse_low'];

// Phone-housed flow: Splash → Setup → Training. Each artboard mounts its own
// instance so reviewers can compare states (e.g. setup vs training) side-by-side.
function TaekyonPhone({ mode, initialScreen = 'splash' }) {
  const theme = resolveTheme('embers', mode);
  const [screen, setScreen] = useStateApp(initialScreen);
  const [state, setState] = useStateApp({
    seconds: 60,
    enabledMoves: DEFAULT_ENABLED,
    legRole: 'both',
  });

  useEffectApp(() => {
    if (document.getElementById('tk-anims')) return;
    const s = document.createElement('style');
    s.id = 'tk-anims';
    s.textContent = `
      @keyframes tk-sway {
        0%, 100% { transform: translate(0, 0) rotate(0deg); }
        25% { transform: translate(-3px, -1px) rotate(-0.8deg); }
        75% { transform: translate(3px, -1px) rotate(0.8deg); }
      }
      @keyframes tk-spin { to { transform: rotate(360deg); } }
    `;
    document.head.appendChild(s);
  }, []);

  let body = null;
  if (screen === 'splash') {
    body = <SplashScreen theme={theme} onContinue={() => setScreen('setup')} />;
  } else if (screen === 'setup') {
    body = <SetupScreen
      theme={theme} state={state} onChange={setState}
      onStart={() => setScreen('training')}
      onBack={() => setScreen('splash')}
    />;
  } else if (screen === 'training') {
    body = <TrainingScreen
      theme={theme} state={state}
      onExit={() => setScreen('setup')}
    />;
  }

  return (
    <div style={{ position: 'relative', width: '100%', height: '100%', background: theme.c.bg }}>
      {body}
      <ScreenTabs screen={screen} setScreen={setScreen} />
    </div>
  );
}

// Tiny dev-helper for jumping between screens — sits at the very bottom of
// each phone, neutral styling so it reads as scaffolding, not part of the app.
function ScreenTabs({ screen, setScreen }) {
  const screens = [
    { id: 'splash',   label: '01' },
    { id: 'setup',    label: '02' },
    { id: 'training', label: '03' },
  ];
  return (
    <div style={{
      position: 'absolute',
      bottom: 6, left: '50%', transform: 'translateX(-50%)',
      zIndex: 100, display: 'flex', gap: 2,
      background: 'rgba(20,20,20,0.78)',
      border: '1px solid rgba(255,255,255,0.12)',
      borderRadius: 999, padding: 2,
      backdropFilter: 'blur(8px)',
    }}>
      {screens.map(s => {
        const active = screen === s.id;
        return (
          <button key={s.id}
            onClick={() => setScreen(s.id)}
            title={s.id}
            style={{
              fontFamily: '"JetBrains Mono", monospace',
              fontSize: 8, letterSpacing: '0.15em',
              padding: '3px 7px',
              background: active ? '#fff' : 'transparent',
              color: active ? '#000' : 'rgba(255,255,255,0.6)',
              border: 'none', borderRadius: 999,
              cursor: 'pointer', fontWeight: 600,
              minWidth: 22,
            }}>{s.label}</button>
        );
      })}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Root
// ─────────────────────────────────────────────────────────────
function App() {
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);
  const mode = t.mode || 'dark';

  return (
    <>
      <DesignCanvas>
        <DCSection id="flow" title="Taekyon Trainer · EMBERS"
          subtitle="Full clickable shell. 01·02·03 chips at the bottom of each phone hop between Splash · Setup · Training. Only roundhouse_low has captured clips today — the rest of the catalog is planned and shown disabled.">
          <DCArtboard id="splash" label="01 · Splash" width={400} height={840}>
            <TaekyonPhone mode={mode} initialScreen="splash" />
          </DCArtboard>
          <DCArtboard id="setup" label="02 · Setup" width={400} height={840}>
            <TaekyonPhone mode={mode} initialScreen="setup" />
          </DCArtboard>
          <DCArtboard id="training" label="03 · Training overlay" width={400} height={840}>
            <TaekyonPhone mode={mode} initialScreen="training" />
          </DCArtboard>
        </DCSection>

        <DCSection id="handoff" title="Handoff spec"
          subtitle="What the Android translator needs. Lift hex values, dp radii, type ramp, component anatomy, and the bridge IO call shape directly into Compose.">
          <DCArtboard id="tokens" label="EMBERS · tokens" width={460} height={1700}>
            <TokensSheet variantId="embers" mode={mode} />
          </DCArtboard>
        </DCSection>
      </DesignCanvas>

      <TweaksPanel>
        <TweakSection label="Theme" />
        <TweakRadio
          label="Mode"
          value={mode}
          options={['dark', 'light']}
          onChange={(v) => setTweak('mode', v)}
        />
      </TweaksPanel>
    </>
  );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
