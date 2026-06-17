// screens.jsx — Splash, Setup, Training. Theme-aware. Each screen reads
// tokens from `theme` (resolveTheme output). Personality cues (mono labels,
// grid bg, sharp vs round corners) are driven by theme.style flags.

const { useState, useEffect, useRef, useMemo } = React;

// ─────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────
function fmtMSS(sec) {
  const s = Math.max(0, Math.floor(sec));
  const m = Math.floor(s / 60);
  const r = s % 60;
  return `${m}:${String(r).padStart(2, '0')}`;
}

function Label({ theme, children, color, size = 11, style = {} }) {
  const mono = theme.style.monoLabels;
  return (
    <div style={{
      fontFamily: theme.fonts.mono,
      fontSize: size,
      fontWeight: mono ? 500 : 600,
      letterSpacing: mono ? '0.18em' : '0.04em',
      textTransform: mono ? 'uppercase' : 'none',
      color: color || theme.c.mute,
      lineHeight: 1.2,
      ...style,
    }}>{children}</div>
  );
}

// Decorative corner brackets — SIGNAL only.
function CornerBrackets({ theme, inset = 12, size = 14 }) {
  if (!theme.style.gridPattern) return null;
  const s = { position: 'absolute', width: size, height: size, borderColor: theme.c.lineStrong, borderStyle: 'solid', pointerEvents: 'none' };
  return (
    <>
      <div style={{ ...s, top: inset, left: inset, borderWidth: '1px 0 0 1px' }} />
      <div style={{ ...s, top: inset, right: inset, borderWidth: '1px 1px 0 0' }} />
      <div style={{ ...s, bottom: inset, left: inset, borderWidth: '0 0 1px 1px' }} />
      <div style={{ ...s, bottom: inset, right: inset, borderWidth: '0 1px 1px 0' }} />
    </>
  );
}

// Skeleton silhouette — placeholder for the Unity scene. Two arms, two legs,
// torso, head. Subtle idle sway via CSS animation.
function SkeletonPlaceholder({ theme, pose = 'idle' }) {
  const c = theme.c.fg;
  const dim = theme.c.mute2;
  return (
    <svg viewBox="0 0 200 360" width="100%" height="100%" preserveAspectRatio="xMidYMid meet"
      style={{ animation: 'tk-sway 4.2s ease-in-out infinite', overflow: 'visible' }}>
      <defs>
        <radialGradient id="tk-floor" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor={theme.c.accent} stopOpacity="0.06" />
          <stop offset="100%" stopColor={theme.c.accent} stopOpacity="0" />
        </radialGradient>
      </defs>
      {/* floor pool */}
      <ellipse cx="100" cy="338" rx="80" ry="10" fill="url(#tk-floor)" />
      {/* head */}
      <circle cx="100" cy="56" r="18" fill="none" stroke={c} strokeWidth="2.5" />
      {/* spine */}
      <line x1="100" y1="76" x2="100" y2="190" stroke={c} strokeWidth="3" strokeLinecap="round" />
      {/* shoulders */}
      <line x1="72" y1="98" x2="128" y2="98" stroke={c} strokeWidth="2.5" strokeLinecap="round" />
      {/* arms — left high guard, right low */}
      <line x1="72" y1="98" x2="56" y2="138" stroke={c} strokeWidth="2.5" strokeLinecap="round" />
      <line x1="56" y1="138" x2="74" y2="166" stroke={c} strokeWidth="2.5" strokeLinecap="round" />
      <line x1="128" y1="98" x2="148" y2="134" stroke={c} strokeWidth="2.5" strokeLinecap="round" />
      <line x1="148" y1="134" x2="138" y2="170" stroke={c} strokeWidth="2.5" strokeLinecap="round" />
      {/* hips */}
      <line x1="82" y1="190" x2="118" y2="190" stroke={c} strokeWidth="2.5" strokeLinecap="round" />
      {/* legs — left forward stance */}
      <line x1="82" y1="190" x2="76" y2="260" stroke={c} strokeWidth="3" strokeLinecap="round" />
      <line x1="76" y1="260" x2="72" y2="330" stroke={c} strokeWidth="3" strokeLinecap="round" />
      <line x1="118" y1="190" x2="128" y2="258" stroke={c} strokeWidth="3" strokeLinecap="round" />
      <line x1="128" y1="258" x2="134" y2="330" stroke={c} strokeWidth="3" strokeLinecap="round" />
      {/* joint dots */}
      {[[72,98],[128,98],[56,138],[148,134],[82,190],[118,190],[76,260],[128,258]].map(([x,y],i) => (
        <circle key={i} cx={x} cy={y} r="2.5" fill={theme.c.accent} />
      ))}
      {/* foot tags */}
      <text x="72" y="346" fontSize="8" fontFamily={theme.fonts.mono} fill={dim} textAnchor="middle" letterSpacing="0.15em">L</text>
      <text x="134" y="346" fontSize="8" fontFamily={theme.fonts.mono} fill={dim} textAnchor="middle" letterSpacing="0.15em">R</text>
    </svg>
  );
}

// Background grid (SIGNAL).
function GridBg({ theme, opacity = 1 }) {
  if (!theme.style.gridPattern) return null;
  return (
    <div style={{
      position: 'absolute', inset: 0, pointerEvents: 'none', opacity,
      backgroundImage:
        `linear-gradient(${theme.c.grid} 1px, transparent 1px),
         linear-gradient(90deg, ${theme.c.grid} 1px, transparent 1px)`,
      backgroundSize: '24px 24px',
    }} />
  );
}

// ─────────────────────────────────────────────────────────────
// SPLASH
// ─────────────────────────────────────────────────────────────
function SplashScreen({ theme, onContinue }) {
  // Boot animation lines (SIGNAL only)
  const [bootIdx, setBootIdx] = useState(0);
  const bootLines = [
    '[ok] motion library mounted · 11 clips',
    '[ok] unity bridge initialized',
    '[ok] state machine: neutral · L · R',
    '[..] awaiting operator',
  ];
  useEffect(() => {
    if (!theme.style.gridPattern) return;
    if (bootIdx >= bootLines.length) return;
    const t = setTimeout(() => setBootIdx(i => i + 1), 320);
    return () => clearTimeout(t);
  }, [bootIdx, theme.id]);

  const sharp = theme.style.cornerStyle === 'sharp';

  return (
    <div style={{
      position: 'absolute', inset: 0, background: theme.c.bg, color: theme.c.fg,
      display: 'flex', flexDirection: 'column',
      fontFamily: theme.fonts.sans,
    }}>
      <GridBg theme={theme} />
      <CornerBrackets theme={theme} inset={16} size={18} />

      {/* Top meta strip */}
      <div style={{
        padding: '20px 22px 0', display: 'flex', justifyContent: 'space-between',
        alignItems: 'baseline', position: 'relative', zIndex: 2,
      }}>
        <Label theme={theme}>{theme.codename} · {theme.name}</Label>
        <Label theme={theme}>{theme.style.buildTag}</Label>
      </div>

      {/* Brand block */}
      <div style={{
        flex: 1, display: 'flex', flexDirection: 'column',
        justifyContent: 'center', padding: '0 28px', position: 'relative', zIndex: 2,
      }}>
        {/* Hangul accent */}
        <div style={{
          fontFamily: theme.fonts.kr,
          fontSize: 22, fontWeight: 500,
          color: theme.c.accent,
          letterSpacing: '0.04em',
          marginBottom: 14,
        }}>결련택견</div>

        {/* Wordmark */}
        <div style={{
          fontFamily: theme.fonts.display,
          fontSize: sharp ? 56 : 64,
          fontWeight: sharp ? 700 : 600,
          lineHeight: 0.92,
          letterSpacing: sharp ? '0.02em' : '-0.04em',
          color: theme.c.fg,
        }}>
          {theme.style.brandWord}
        </div>

        {/* Underline rule */}
        <div style={{
          marginTop: sharp ? 22 : 16,
          height: sharp ? 1 : 2,
          width: sharp ? '100%' : 48,
          background: sharp ? theme.c.line : theme.c.accent,
        }} />

        {/* Subtitle */}
        <div style={{
          marginTop: 14,
          fontFamily: sharp ? theme.fonts.mono : theme.fonts.sans,
          fontSize: sharp ? 12 : 17,
          fontWeight: sharp ? 500 : 400,
          letterSpacing: sharp ? '0.22em' : '0',
          textTransform: sharp ? 'uppercase' : 'none',
          color: theme.c.mute,
        }}>
          {sharp ? theme.style.brandSub : `${theme.style.brandSub} · Korean martial reaction drill`}
        </div>

        {/* Boot log — SIGNAL only */}
        {sharp && (
          <div style={{
            marginTop: 40,
            fontFamily: theme.fonts.mono, fontSize: 11,
            color: theme.c.mute, lineHeight: 1.9,
            minHeight: 90,
          }}>
            {bootLines.slice(0, bootIdx).map((l, i) => (
              <div key={i} style={{
                color: l.startsWith('[ok]') ? theme.c.accent : theme.c.mute,
              }}>{l}<span style={{ color: theme.c.mute2 }}>{i === bootIdx - 1 ? ' ▍' : ''}</span></div>
            ))}
          </div>
        )}

        {/* Decorative dossier card — EMBERS only */}
        {!sharp && (
          <div style={{
            marginTop: 40,
            background: theme.c.surface,
            borderRadius: theme.radius.lg,
            padding: '18px 18px 16px',
            border: `1px solid ${theme.c.line}`,
          }}>
            <Label theme={theme} size={10}>About</Label>
            <div style={{
              marginTop: 8, fontSize: 13, lineHeight: 1.55,
              color: theme.c.mute,
              fontFamily: theme.fonts.sans,
            }}>
              Watch the opponent. React in real time.<br/>
              No scoring. No pose tracking. Just rhythm.
            </div>
          </div>
        )}
      </div>

      {/* CTA */}
      <div style={{ padding: '0 22px 30px', position: 'relative', zIndex: 2 }}>
        <button onClick={onContinue} style={{
          width: '100%',
          height: sharp ? 54 : 60,
          background: theme.c.accent,
          color: theme.c.accentInk,
          border: 'none',
          borderRadius: sharp ? theme.radius.sm : theme.radius.pill,
          fontFamily: sharp ? theme.fonts.mono : theme.fonts.sans,
          fontSize: sharp ? 13 : 16,
          fontWeight: sharp ? 600 : 600,
          letterSpacing: sharp ? '0.24em' : '0.01em',
          textTransform: sharp ? 'uppercase' : 'none',
          cursor: 'pointer',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          gap: 10,
        }}>
          {sharp ? '[ INITIALIZE → ]' : 'Begin training'}
        </button>

        {sharp && (
          <div style={{
            marginTop: 12, textAlign: 'center',
            fontFamily: theme.fonts.mono, fontSize: 10,
            color: theme.c.mute2, letterSpacing: '0.2em',
          }}>TAP TO CONTINUE</div>
        )}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// SETUP
// ─────────────────────────────────────────────────────────────
function SetupScreen({ theme, state, onChange, onStart, onBack }) {
  const sharp = theme.style.cornerStyle === 'sharp';
  const { seconds, enabledMoves } = state;
  const enabled = new Set(enabledMoves);
  const canStart = enabled.size > 0;

  const toggle = (id) => {
    const next = new Set(enabled);
    if (next.has(id)) next.delete(id); else next.add(id);
    onChange({ ...state, enabledMoves: [...next] });
  };

  return (
    <div style={{
      position: 'absolute', inset: 0, background: theme.c.bg, color: theme.c.fg,
      fontFamily: theme.fonts.sans,
      display: 'flex', flexDirection: 'column', overflow: 'hidden',
    }}>
      <GridBg theme={theme} opacity={0.6} />

      {/* Header */}
      <div style={{
        padding: '18px 20px 16px',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        position: 'relative', zIndex: 2,
        borderBottom: sharp ? `1px solid ${theme.c.line}` : 'none',
      }}>
        <button onClick={onBack} style={{
          background: 'transparent', border: 'none', cursor: 'pointer',
          width: 32, height: 32, display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: theme.c.fg, marginLeft: -8,
        }}>
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M12.5 4L6.5 10L12.5 16" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </button>

        <div style={{ textAlign: 'center', flex: 1 }}>
          <div style={{
            fontFamily: theme.fonts.sans,
            fontSize: 15, fontWeight: 600, color: theme.c.fg,
            letterSpacing: sharp ? '0.04em' : '-0.01em',
          }}>
            {sharp ? 'SESSION SETUP' : 'Session setup'}
          </div>
          <div style={{
            fontFamily: theme.fonts.kr, fontSize: 10,
            color: theme.c.mute2, marginTop: 2,
          }}>훈련 준비</div>
        </div>

        <div style={{ width: 32 }} />
      </div>

      {/* Scrollable body */}
      <div style={{
        flex: 1, overflow: 'auto', padding: '20px 20px 16px',
        position: 'relative', zIndex: 2,
      }}>
        <SectionDuration theme={theme} state={state} onChange={onChange} />
        <SectionTechniques theme={theme} enabled={enabled} toggle={toggle} />
      </div>

      {/* Footer: Start CTA */}
      <div style={{
        padding: '14px 20px 22px',
        borderTop: sharp ? `1px solid ${theme.c.line}` : 'none',
        background: theme.c.bg, position: 'relative', zIndex: 3,
      }}>
        {sharp && (
          <div style={{
            display: 'flex', justifyContent: 'space-between',
            marginBottom: 12, fontFamily: theme.fonts.mono,
            fontSize: 10, letterSpacing: '0.2em', color: theme.c.mute2,
          }}>
            <span>READY</span>
            <span>{fmtMSS(seconds)} · {enabled.size} VARIANT{enabled.size === 1 ? '' : 'S'}</span>
          </div>
        )}
        <button
          onClick={canStart ? onStart : undefined}
          disabled={!canStart}
          style={{
            width: '100%', height: sharp ? 54 : 60,
            background: canStart ? theme.c.accent : theme.c.surface2,
            color: canStart ? theme.c.accentInk : theme.c.mute2,
            border: 'none',
            borderRadius: sharp ? theme.radius.sm : theme.radius.pill,
            fontFamily: sharp ? theme.fonts.mono : theme.fonts.sans,
            fontSize: sharp ? 13 : 16,
            fontWeight: 600,
            letterSpacing: sharp ? '0.24em' : '0.01em',
            textTransform: sharp ? 'uppercase' : 'none',
            cursor: canStart ? 'pointer' : 'not-allowed',
            display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
          }}>
          {sharp
            ? (canStart ? '[ START SESSION → ]' : '[ ENABLE ≥1 VARIANT ]')
            : (canStart ? `Start · ${fmtMSS(seconds)}` : 'Pick at least one variant')}
        </button>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// SETUP · Duration section
// ─────────────────────────────────────────────────────────────
function SectionDuration({ theme, state, onChange }) {
  const sharp = theme.style.cornerStyle === 'sharp';
  const { seconds } = state;
  const setSec = (s) => onChange({ ...state, seconds: Math.max(15, Math.min(300, s)) });

  return (
    <div style={{ marginBottom: 26 }}>
      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 12 }}>
        <Label theme={theme} size={11} style={{ color: theme.c.mute }}>
          {sharp ? '⟶ DURATION' : 'Duration'}
        </Label>
        <Label theme={theme} size={10} style={{ color: theme.c.mute2 }}>
          {sharp ? `M:SS · 0:30–5:00` : '30s – 5m'}
        </Label>
      </div>

      {/* Big number display */}
      <div style={{
        background: sharp ? 'transparent' : theme.c.surface,
        border: `1px solid ${theme.c.line}`,
        borderRadius: sharp ? theme.radius.sm : theme.radius.lg,
        padding: sharp ? '14px 16px 16px' : '20px 22px',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        marginBottom: 10,
      }}>
        <button onClick={() => setSec(seconds - 15)} style={stepperBtn(theme)}>–15s</button>
        <div style={{
          fontFamily: theme.fonts.mono,
          fontSize: sharp ? 52 : 64,
          fontWeight: 600, fontVariantNumeric: 'tabular-nums',
          letterSpacing: sharp ? '0.02em' : '-0.04em',
          color: theme.c.fg, lineHeight: 1,
        }}>{fmtMSS(seconds)}</div>
        <button onClick={() => setSec(seconds + 15)} style={stepperBtn(theme)}>+15s</button>
      </div>

      {/* Presets */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: 6 }}>
        {DURATION_PRESETS.map(p => {
          const active = seconds === p.seconds;
          return (
            <button key={p.seconds} onClick={() => setSec(p.seconds)} style={{
              height: 36,
              background: active ? theme.c.accent : 'transparent',
              color: active ? theme.c.accentInk : theme.c.fg,
              border: `1px solid ${active ? theme.c.accent : theme.c.line}`,
              borderRadius: sharp ? theme.radius.sm : theme.radius.md,
              fontFamily: theme.fonts.mono,
              fontSize: 12, fontWeight: 600,
              fontVariantNumeric: 'tabular-nums',
              cursor: 'pointer',
              letterSpacing: '0.04em',
            }}>{p.label}</button>
          );
        })}
      </div>
    </div>
  );
}

function stepperBtn(theme) {
  const sharp = theme.style.cornerStyle === 'sharp';
  return {
    background: 'transparent',
    border: `1px solid ${theme.c.line}`,
    color: theme.c.mute,
    width: 56, height: 32,
    borderRadius: sharp ? theme.radius.sm : theme.radius.md,
    fontFamily: theme.fonts.mono, fontSize: 11, fontWeight: 500,
    cursor: 'pointer', letterSpacing: '0.04em',
  };
}

// ─────────────────────────────────────────────────────────────
// SETUP · Techniques section
// ─────────────────────────────────────────────────────────────
function SectionTechniques({ theme, enabled, toggle }) {
  const sharp = theme.style.cornerStyle === 'sharp';
  const totalVariants = TECHNIQUES.reduce(
    (n, t) => n + t.heights.filter(h => h.status === 'ready').length, 0
  );
  return (
    <div>
      <div style={{
        display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
        marginBottom: 12,
      }}>
        <Label theme={theme} size={11} style={{ color: theme.c.mute }}>
          {sharp ? '⟶ TECHNIQUES' : 'Techniques'}
        </Label>
        <Label theme={theme} size={10} style={{ color: theme.c.mute2 }}>
          {sharp ? `${enabled.size} / ${totalVariants} ENABLED` : `${enabled.size} selected`}
        </Label>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: sharp ? 0 : 10 }}>
        {TECHNIQUES.map((t, i) => (
          <TechniqueRow
            key={t.id}
            theme={theme}
            tech={t}
            enabled={enabled}
            toggle={toggle}
            first={i === 0}
            last={i === TECHNIQUES.length - 1}
          />
        ))}
      </div>

      {/* Footer hint */}
      <div style={{
        marginTop: 16, padding: '12px 14px',
        border: `1px dashed ${theme.c.line}`,
        borderRadius: sharp ? theme.radius.sm : theme.radius.md,
        display: 'flex', alignItems: 'flex-start', gap: 10,
        fontFamily: theme.fonts.mono,
        fontSize: 10, color: theme.c.mute2,
        letterSpacing: sharp ? '0.1em' : '0.02em', lineHeight: 1.5,
      }}>
        <span style={{ color: theme.c.accent }}>◆</span>
        <span>{sharp
          ? 'NEW VARIANTS UNLOCK AS MOTIONS ARE CAPTURED. CURRENT BUILD: 1 READY · 7 PLANNED.'
          : 'New variants unlock as motion clips are captured. Current build: 1 ready · 7 planned.'
        }</span>
      </div>
    </div>
  );
}

// One row per technique family. Ready families show the full card with
// height chips. Planned families collapse to a compact single-line summary
// so they don't dominate the scroll — they exist to show the roadmap, not
// to be configured.
function TechniqueRow({ theme, tech, enabled, toggle, first }) {
  const sharp = theme.style.cornerStyle === 'sharp';
  const familyReady = tech.status === 'ready';
  const anyOn = tech.heights.some(h => enabled.has(`${h.id}_front`) || enabled.has(`${h.id}_rear`));

  // Compact row for not-yet-captured techniques.
  if (!familyReady) {
    return (
      <div style={{
        padding: sharp ? '10px 4px' : '12px 14px',
        background: sharp ? 'transparent' : theme.c.surface,
        border: sharp ? `1px solid ${theme.c.line}` : `1px solid ${theme.c.line}`,
        borderTopWidth: sharp && !first ? 0 : 1,
        borderRadius: sharp ? 0 : theme.radius.lg,
        display: 'flex', alignItems: 'baseline', gap: 10,
        opacity: 0.55,
      }}>
        <div style={{
          fontFamily: theme.fonts.sans, fontSize: 14, fontWeight: 600,
          color: theme.c.fg,
        }}>{tech.name}</div>
        <div style={{
          fontFamily: theme.fonts.kr, fontSize: 12, color: theme.c.mute,
        }}>{tech.hangul}</div>
        <div style={{
          fontFamily: theme.fonts.mono, fontSize: 10,
          color: theme.c.mute2, letterSpacing: '0.05em',
        }}>· {tech.romaja}</div>
        <span style={{
          marginLeft: 'auto',
          fontFamily: theme.fonts.mono, fontSize: 9,
          color: theme.c.mute2, letterSpacing: '0.22em',
          border: `1px solid ${theme.c.line}`,
          borderRadius: sharp ? 2 : 4,
          padding: '2px 6px', textTransform: 'uppercase',
          flexShrink: 0,
        }}>{sharp ? 'PLANNED' : 'soon'}</span>
      </div>
    );
  }

  // Full card for ready techniques.
  return (
    <div
      style={{
        padding: sharp ? '14px 4px' : '14px 14px',
        background: sharp ? 'transparent' : (anyOn ? theme.c.accentDim : theme.c.surface),
        border: sharp
          ? `1px solid ${theme.c.line}`
          : `1px solid ${anyOn ? theme.c.accent : theme.c.line}`,
        borderTopWidth: sharp && !first ? 0 : 1,
        borderRadius: sharp ? 0 : theme.radius.lg,
        position: 'relative',
      }}
    >
      {/* Title row */}
      <div style={{
        display: 'flex', alignItems: 'baseline', gap: 10,
        flexWrap: 'wrap', marginBottom: 6,
      }}>
        <div style={{
          fontFamily: theme.fonts.sans,
          fontSize: 16, fontWeight: 600, color: theme.c.fg,
          letterSpacing: sharp ? '0.01em' : '-0.01em',
        }}>{tech.name}</div>
        <div style={{
          fontFamily: theme.fonts.kr, fontSize: 13, color: theme.c.mute,
        }}>{tech.hangul}</div>
        <div style={{
          fontFamily: theme.fonts.mono, fontSize: 10,
          color: theme.c.mute2, letterSpacing: '0.05em',
        }}>· {tech.romaja}</div>
      </div>

      {/* Description */}
      <div style={{
        fontFamily: theme.fonts.sans, fontSize: 12, lineHeight: 1.4,
        color: theme.c.mute, marginBottom: 12,
      }}>{tech.desc}</div>

      {/* Per-height front / rear chips */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {tech.heights.map(h => (
          <HeightRow key={h.id} theme={theme} chip={h} enabled={enabled} toggle={toggle} />
        ))}
      </div>
    </div>
  );
}

function HeightRow({ theme, chip, enabled, toggle }) {
  const sharp = theme.style.cornerStyle === 'sharp';
  const frontId = `${chip.id}_front`;
  const rearId  = `${chip.id}_rear`;
  const ready   = chip.status === 'ready';
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <div style={{
        width: 36, flexShrink: 0,
        fontFamily: theme.fonts.mono, fontSize: 10,
        color: theme.c.mute, letterSpacing: '0.06em',
        textTransform: sharp ? 'uppercase' : 'none',
      }}>
        {sharp ? chip.label.toUpperCase() : chip.label}
      </div>
      {[{ id: frontId, label: '앞발' }, { id: rearId, label: '뒷발' }].map(opt => {
        const on = enabled.has(opt.id);
        return (
          <button key={opt.id}
            onClick={() => ready && toggle(opt.id)}
            disabled={!ready}
            style={{
              all: 'unset',
              cursor: ready ? 'pointer' : 'not-allowed',
              display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
              padding: '5px 12px',
              background: on ? theme.c.accent : 'transparent',
              color: on ? theme.c.accentInk : (ready ? theme.c.fg : theme.c.mute2),
              border: `1px solid ${on ? theme.c.accent : (ready ? theme.c.lineStrong : theme.c.line)}`,
              borderRadius: sharp ? theme.radius.sm : theme.radius.md,
              fontFamily: theme.fonts.kr,
              fontSize: 12, fontWeight: 600,
              opacity: ready ? 1 : 0.5,
            }}
          >{opt.label}</button>
        );
      })}
    </div>
  );
}

function HeightChip({ theme, chip, on, onToggle }) {
  const sharp = theme.style.cornerStyle === 'sharp';
  const ready = chip.status === 'ready';
  const labelText = sharp ? chip.label.toUpperCase() : chip.label;

  return (
    <button
      onClick={ready ? onToggle : undefined}
      disabled={!ready}
      style={{
        all: 'unset',
        cursor: ready ? 'pointer' : 'not-allowed',
        display: 'inline-flex', alignItems: 'center', gap: 8,
        padding: '6px 10px 6px 8px',
        background: on ? theme.c.accent : 'transparent',
        color: on ? theme.c.accentInk : (ready ? theme.c.fg : theme.c.mute2),
        border: `1px solid ${on ? theme.c.accent : (ready ? theme.c.lineStrong : theme.c.line)}`,
        borderRadius: sharp ? theme.radius.sm : theme.radius.md,
        fontFamily: theme.fonts.sans,
        fontSize: 12, fontWeight: 600,
        letterSpacing: sharp ? '0.08em' : '0',
        opacity: ready ? 1 : 0.6,
        boxSizing: 'border-box',
      }}
    >
      {/* dot indicator */}
      <span style={{
        width: 14, height: 14, flexShrink: 0,
        border: `1.5px solid ${on ? theme.c.accentInk : (ready ? theme.c.lineStrong : theme.c.line)}`,
        background: on ? theme.c.accentInk : 'transparent',
        borderRadius: sharp ? 2 : 4,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>
        {on && (
          <svg width="9" height="9" viewBox="0 0 12 12" fill="none">
            <path d="M2.5 6.5L5 9L9.5 3.5" stroke={theme.c.accent} strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        )}
      </span>
      <span>{labelText}</span>
      <span style={{
        fontFamily: theme.fonts.mono,
        fontSize: 9, fontWeight: 500,
        letterSpacing: '0.1em',
        color: on ? theme.c.accentInk : theme.c.mute2,
        opacity: 0.75,
      }}>{ready ? `· ${chip.variants}V` : '· SOON'}</span>
    </button>
  );
}

// ─────────────────────────────────────────────────────────────
// TRAINING — full-bleed Unity scene + edge-hugging chrome
// ─────────────────────────────────────────────────────────────
function TrainingScreen({ theme, state, onExit, onComplete }) {
  const sharp = theme.style.cornerStyle === 'sharp';
  const { seconds, enabledMoves } = state;
  const [remaining, setRemaining] = useState(seconds);
  const [unityReady, setUnityReady] = useState(false);
  const [overTime, setOverTime] = useState(false);
  const [kickCounter, setKickCounter] = useState(0);
  const startedRef = useRef(false);

  // Simulate the Unity onSceneReady handshake (250ms delay), then begin timer.
  useEffect(() => {
    setRemaining(seconds);
    setOverTime(false);
    setUnityReady(false);
    startedRef.current = false;
    const t = setTimeout(() => {
      setUnityReady(true);
      startedRef.current = true;
    }, 700);
    return () => clearTimeout(t);
  }, [seconds]);

  // Timer tick.
  useEffect(() => {
    if (!unityReady) return;
    const t = setInterval(() => {
      setRemaining(r => {
        if (r <= 0) {
          setOverTime(true);
          return 0;
        }
        return r - 1;
      });
    }, 1000);
    return () => clearInterval(t);
  }, [unityReady]);

  // Simulated kick fire rate for the placeholder counter.
  useEffect(() => {
    if (!unityReady || overTime) return;
    const t = setInterval(() => setKickCounter(k => k + 1), 1800);
    return () => clearInterval(t);
  }, [unityReady, overTime]);

  const timerColor = overTime ? theme.c.warn : (remaining < 10 ? theme.c.warn : theme.c.fg);

  return (
    <div style={{
      position: 'absolute', inset: 0,
      background: theme.c.bg, color: theme.c.fg,
      fontFamily: theme.fonts.sans, overflow: 'hidden',
    }}>
      {/* Unity scene placeholder — full bleed */}
      <UnityViewport theme={theme} ready={unityReady} overTime={overTime} />

      {/* Top edge chrome */}
      <div style={{
        position: 'absolute', top: 0, left: 0, right: 0,
        padding: '14px 14px 0',
        display: 'flex', alignItems: 'flex-start',
        justifyContent: 'space-between', gap: 10,
        zIndex: 5, pointerEvents: 'none',
      }}>
        {/* Exit button — top-left */}
        <button onClick={onExit} style={{
          pointerEvents: 'auto',
          width: 44, height: 44,
          background: overTime ? theme.c.warn : theme.c.surface,
          border: `1px solid ${overTime ? theme.c.warn : theme.c.line}`,
          borderRadius: sharp ? theme.radius.sm : theme.radius.pill,
          color: overTime ? theme.c.accentInk : theme.c.fg,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          cursor: 'pointer', backdropFilter: 'blur(8px)',
        }}>
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M4 4L12 12M12 4L4 12" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round"/>
          </svg>
        </button>

        {/* Timer — center */}
        <div style={{
          pointerEvents: 'auto',
          display: 'flex', flexDirection: 'column', alignItems: 'center',
          padding: sharp ? '8px 18px 9px' : '8px 22px 10px',
          background: theme.c.surface,
          border: `1px solid ${overTime ? theme.c.warn : theme.c.line}`,
          borderRadius: sharp ? theme.radius.sm : theme.radius.pill,
          backdropFilter: 'blur(8px)',
          minWidth: 140,
        }}>
          <Label theme={theme} size={9} style={{ color: overTime ? theme.c.warn : theme.c.mute2, marginBottom: 2 }}>
            {overTime ? (sharp ? '◆ TIME EXPIRED' : 'Time up · keep watching') : (unityReady ? (sharp ? '● ACTIVE' : 'Active') : (sharp ? '○ LOADING' : 'Loading...'))}
          </Label>
          <div style={{
            fontFamily: theme.fonts.mono,
            fontSize: sharp ? 26 : 30,
            fontWeight: 600, fontVariantNumeric: 'tabular-nums',
            letterSpacing: sharp ? '0.04em' : '-0.02em',
            color: timerColor, lineHeight: 1,
          }}>{fmtMSS(remaining)}</div>
        </div>

        {/* Right slot: technique counter (subtle) */}
        <div style={{
          pointerEvents: 'auto',
          minWidth: 44, height: 44,
          background: theme.c.surface,
          border: `1px solid ${theme.c.line}`,
          borderRadius: sharp ? theme.radius.sm : theme.radius.pill,
          display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center',
          padding: '0 10px', backdropFilter: 'blur(8px)',
        }}>
          <div style={{
            fontFamily: theme.fonts.mono, fontSize: 9,
            color: theme.c.mute2, letterSpacing: '0.15em',
          }}>{sharp ? 'HITS' : 'kicks'}</div>
          <div style={{
            fontFamily: theme.fonts.mono, fontSize: 14, fontWeight: 600,
            color: overTime ? theme.c.warn : theme.c.accent, lineHeight: 1.1,
            fontVariantNumeric: 'tabular-nums',
          }}>{String(kickCounter).padStart(2, '0')}</div>
        </div>
      </div>

      {/* Bottom edge chrome — minimal */}
      <div style={{
        position: 'absolute', bottom: 14, left: 14, right: 14,
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        fontFamily: theme.fonts.mono, fontSize: 10,
        color: theme.c.mute2, letterSpacing: '0.18em',
        zIndex: 5, pointerEvents: 'none',
      }}>
        <span>{sharp ? `UNITY · ${unityReady ? 'CONNECTED' : 'HANDSHAKE…'}` : ''}</span>
        <span>{sharp ? enabledMoves.slice(0, 2).map(m => m.toUpperCase()).join(' · ') + (enabledMoves.length > 2 ? ` +${enabledMoves.length - 2}` : '') : ''}</span>
      </div>

      {/* Loading veil */}
      {!unityReady && (
        <div style={{
          position: 'absolute', inset: 0,
          background: theme.c.bg + 'dd',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          flexDirection: 'column', gap: 14,
          backdropFilter: 'blur(4px)',
          zIndex: 4,
        }}>
          <div style={{
            width: 40, height: 40,
            border: `2px solid ${theme.c.line}`,
            borderTopColor: theme.c.accent,
            borderRadius: '50%',
            animation: 'tk-spin 0.9s linear infinite',
          }} />
          <Label theme={theme} size={11}>{sharp ? 'BOOTING UNITY SCENE' : 'Preparing opponent…'}</Label>
        </div>
      )}
    </div>
  );
}

// Unity viewport placeholder — labelled clearly so the dev plugs Unity here.
function UnityViewport({ theme, ready, overTime }) {
  const sharp = theme.style.cornerStyle === 'sharp';
  const fade = overTime ? 0.45 : 1;

  return (
    <div style={{
      position: 'absolute', inset: 0,
      display: 'flex', flexDirection: 'column',
      filter: overTime ? 'grayscale(0.4)' : 'none',
      transition: 'filter .4s',
    }}>
      {/* Stripe pattern label */}
      <div style={{
        position: 'absolute', top: 70, left: 14,
        fontFamily: theme.fonts.mono, fontSize: 9,
        color: theme.c.mute2, letterSpacing: '0.2em', zIndex: 3,
      }}>{sharp ? '/// UNITY VIEWPORT' : ''}</div>

      {/* Scene background — subtle vertical lines representing depth */}
      <div style={{
        position: 'absolute', inset: 0,
        backgroundImage: sharp
          ? `repeating-linear-gradient(180deg, transparent 0 38px, ${theme.c.grid} 38px 39px)`
          : `radial-gradient(ellipse at 50% 70%, ${theme.c.accentDim} 0%, transparent 60%)`,
        opacity: fade,
      }} />

      {/* Floor reference grid (perspective trick — 4 lines fanning out) */}
      <svg viewBox="0 0 200 360" preserveAspectRatio="none" style={{
        position: 'absolute', inset: 0, width: '100%', height: '100%',
        opacity: fade * 0.4,
      }}>
        {/* horizon */}
        <line x1="0" y1="240" x2="200" y2="240" stroke={theme.c.line} strokeWidth="0.5"/>
        {/* receding floor lines */}
        {[0, 0.25, 0.5, 0.75, 1].map((t, i) => (
          <line key={i}
            x1={t * 200} y1="360"
            x2={100} y2="240"
            stroke={theme.c.line} strokeWidth="0.5"/>
        ))}
        {/* depth bars */}
        {[260, 280, 300, 325].map((y, i) => (
          <line key={i} x1="0" y1={y} x2="200" y2={y} stroke={theme.c.line} strokeWidth="0.5" opacity={0.6 - i * 0.1}/>
        ))}
      </svg>

      {/* Skeleton silhouette */}
      <div style={{
        position: 'absolute', left: '50%', top: '52%',
        transform: 'translate(-50%, -50%)',
        width: '62%', height: '70%', opacity: fade,
      }}>
        <SkeletonPlaceholder theme={theme} />
      </div>
    </div>
  );
}

Object.assign(window, { SplashScreen, SetupScreen, TrainingScreen, fmtMSS });
