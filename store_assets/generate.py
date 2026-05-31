"""Generate Play Store assets using Pillow."""

from PIL import Image, ImageDraw, ImageFont
import os

OUT = os.path.dirname(os.path.abspath(__file__))

# ── Palette ───────────────────────────────────────────────────────────────────
BG          = (16,  12,   8)
AMBER       = (255, 170,  43)
AMBER_DIM   = (232, 136,  31)
AMBER_MED   = (190, 125,  35)
AMBER_MUTED = (120,  80,  20)
AMBER_FAINT = ( 50,  35,  10)

# ── Helpers ───────────────────────────────────────────────────────────────────
def rstroke(draw, pts, color, w):
    """Polyline with round caps and joins."""
    w = max(2, w)
    draw.line(pts, fill=color, width=w, joint="curve")
    r = w // 2
    for x, y in pts:
        draw.ellipse([x - r, y - r, x + r, y + r], fill=color)

def bezier_pts(p0, ctrl, p1, steps=16):
    out = []
    for i in range(steps + 1):
        t = i / steps
        x = (1-t)**2*p0[0] + 2*(1-t)*t*ctrl[0] + t**2*p1[0]
        y = (1-t)**2*p0[1] + 2*(1-t)*t*ctrl[1] + t**2*p1[1]
        out.append((round(x), round(y)))
    return out

def draw_arc(img, sc, ox, oy):
    """Motion-trail arc with alpha fade, composited onto img."""
    seg1 = bezier_pts((96,388), (150,320), (214,250))
    seg2 = bezier_pts((214,250), (290,200), (340,142))
    pts  = seg1 + seg2[1:]
    n    = len(pts) - 1
    ov   = Image.new("RGBA", img.size, (0,0,0,0))
    d    = ImageDraw.Draw(ov)
    w    = max(2, round(22 * sc))
    for i in range(n):
        a  = int(190 * (i / n))
        x1 = round(pts[i][0]   * sc + ox); y1 = round(pts[i][1]   * sc + oy)
        x2 = round(pts[i+1][0] * sc + ox); y2 = round(pts[i+1][1] * sc + oy)
        d.line([(x1,y1),(x2,y2)], fill=(255,170,43,a), width=w)
    return Image.alpha_composite(img.convert("RGBA"), ov).convert("RGB")

def draw_figure(draw, sc, ox, oy):
    """Kicking figure. sc=scale, ox/oy=pixel offset from origin."""
    s  = lambda w: max(2, round(w * sc))
    tp = lambda pts: [(round(x*sc+ox), round(y*sc+oy)) for x,y in pts]

    rstroke(draw, tp([(190,182),(150,206),(114,196)]), AMBER_DIM, s(20))  # trailing arm
    rstroke(draw, tp([(198,180),(248,178),(292,162)]), AMBER_DIM, s(20))  # lead arm
    rstroke(draw, tp([(214,250),(184,322),(250,344)]), AMBER,     s(42))  # tucked leg
    rstroke(draw, tp([(214,250),(276,192),(342,140)]), AMBER,     s(46))  # kicking leg
    rstroke(draw, tp([(184,170),(214,250)]),           AMBER,     s(44))  # torso
    cx = round(166*sc + ox); cy = round(138*sc + oy); r = round(29*sc)
    draw.ellipse([cx-r, cy-r, cx+r, cy+r], fill=AMBER)                   # head

def load_font(candidates, size):
    for path in candidates:
        try:
            return ImageFont.truetype(path, size)
        except Exception:
            pass
    return ImageFont.load_default()

BOLD = lambda sz: load_font([
    "C:/Windows/Fonts/seguisb.ttf",
    "C:/Windows/Fonts/segoeuib.ttf",
    "C:/Windows/Fonts/arialbd.ttf",
    "C:/Windows/Fonts/arial.ttf",
], sz)

REGULAR = lambda sz: load_font([
    "C:/Windows/Fonts/segoeui.ttf",
    "C:/Windows/Fonts/arial.ttf",
], sz)

MONO = lambda sz: load_font([
    "C:/Windows/Fonts/consola.ttf",
    "C:/Windows/Fonts/cour.ttf",
], sz)

KO = lambda sz: load_font([
    "C:/Windows/Fonts/malgun.ttf",
    "C:/Windows/Fonts/malgunbd.ttf",
    "C:/Windows/Fonts/gulim.ttc",
    "C:/Windows/Fonts/segoeui.ttf",
], sz)

# ── Icon 512×512 ──────────────────────────────────────────────────────────────
def make_icon():
    img  = Image.new("RGB", (512, 512), BG)
    sc   = 512 / 432          # original viewport → canvas
    img  = draw_arc(img, sc, 0, 0)
    draw = ImageDraw.Draw(img)
    draw_figure(draw, sc, 0, 0)
    return img

# ── Feature graphic 1024×500 ──────────────────────────────────────────────────
def make_feature():
    W, H = 1024, 500
    img  = Image.new("RGB", (W, H), BG)

    # Figure: original center ≈ (225, 227), map to canvas (225, 258), scale 1.28
    sc = 1.28
    ox = round(225 - 225 * sc)   # −63
    oy = round(258 - 227 * sc)   # −33
    img  = draw_arc(img, sc, ox, oy)
    draw = ImageDraw.Draw(img)
    draw_figure(draw, sc, ox, oy)

    # Top and bottom border strokes
    draw.line([(0, 0),    (W, 0)],    fill=AMBER_FAINT, width=2)
    draw.line([(0, H-1),  (W, H-1)],  fill=AMBER_FAINT, width=2)

    # Vertical divider
    for y in range(62, 440):
        alpha = int(60 * min(1, min(y-62, 440-y) / 40))
        draw.point((468, y), fill=(
            int(BG[0] + (AMBER[0]-BG[0]) * alpha/60),
            int(BG[1] + (AMBER[1]-BG[1]) * alpha/60),
            int(BG[2] + (AMBER[2]-BG[2]) * alpha/60),
        ))

    # ── Text ──────────────────────────────────────────────────────────────────
    tx = 506   # left edge of text column

    # Korean art name — small badge
    draw.text((tx, 98),  "결련택견", font=KO(14),   fill=AMBER_MUTED)

    # App name — two lines
    f_big = BOLD(86)
    draw.text((tx, 130), "Taekyun", font=f_big, fill=AMBER)
    draw.text((tx, 222), "Trainer", font=f_big, fill=AMBER_MED)

    # Hairline rule
    draw.line([(tx, 326), (928, 326)], fill=AMBER_FAINT, width=1)

    # Korean app name
    draw.text((tx, 342), "택견 트레이너", font=KO(22), fill=AMBER_MED)

    # Tagline
    draw.text((tx, 390), "WATCH  ·  REACT  ·  FLOW",
              font=MONO(13), fill=AMBER_MUTED)

    return img

# ── Export ────────────────────────────────────────────────────────────────────
icon    = make_icon()
feature = make_feature()

icon.save(   os.path.join(OUT, "icon_512.png"),                  "PNG")
feature.save(os.path.join(OUT, "feature_graphic_1024x500.png"),  "PNG")
feature.save(os.path.join(OUT, "feature_graphic_1024x500.jpg"),  "JPEG", quality=95)

print("Saved:")
print(f"  {OUT}/icon_512.png")
print(f"  {OUT}/feature_graphic_1024x500.png")
print(f"  {OUT}/feature_graphic_1024x500.jpg")
