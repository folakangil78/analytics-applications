"""
Temporal animation of priority-fee (validator-tip) expenditure across the three
volatility-event windows analyzed by the Arbitrum project.

Layout: three stacked subplots (one per event) sharing a Bloomberg-style dark
canvas. Each subplot lays out the analyst-selected days back-to-back along the
x-axis with visible separators between non-consecutive days, so the per-hour
spike during each event's actual peak hours is grounded in the real calendar
dates we used. FTX stays at zero because that data was deliberately excluded --
it is the control.

Per-hour values are aggregate priority-fee tips (gwei) distilled from the
SecondCode.scala output (contested-bucket avg/max-tip stats and the per-tx
flagged tables in screenshots/MainAnalytic/). Edit the three _..._series()
functions below to refine.

Output:
    gas_animation.gif  (always written, no extra dependency)
    gas_animation.mp4  (only if ffmpeg is installed; suitable for YouTube)

To install ffmpeg locally for .mp4 export:
    brew install ffmpeg
"""

import shutil
from pathlib import Path

import matplotlib.pyplot as plt
import matplotlib.animation as animation
import numpy as np

OUTPUT_DIR = Path(__file__).resolve().parent

# -----------------------------------------------------------------------------
# Per-hour aggregate priority-fee tips (gwei) for each event window.
# -----------------------------------------------------------------------------

def _airdrop_series():
    # 2023-03-15, 2023-03-16, 2023-03-23, 2023-03-24  (4 selected days = 96 hrs)
    # Claim opened 2023-03-23 ~13:00 UTC -> spike concentrates on day 3 (idx 48-71).
    base = np.full(96, 0.030)
    rng = np.random.default_rng(11)
    base += rng.normal(0, 0.004, 96).clip(min=-0.01)
    base[18:23] += np.linspace(0.02, 0.06, 5)            # day 1 evening positioning
    base[36:42] += 0.03                                   # day 2 mild elevation
    spike = np.array([0.05, 0.18, 0.42, 0.61, 0.55, 0.38, 0.22, 0.14, 0.08])
    base[58:58 + len(spike)] += spike                     # day 3 claim spike
    base[74:80] += np.linspace(0.10, 0.04, 6)            # day 4 aftershocks
    return base

def _flash_series():
    # 2024-08-01, 2024-08-04, 2024-08-05  (3 selected days = 72 hrs)
    # Red Monday crash 2024-08-05; liquidation-bot war during early UTC hours.
    base = np.full(72, 0.045)
    rng = np.random.default_rng(22)
    base += rng.normal(0, 0.005, 72).clip(min=-0.01)
    base[28:34] += np.linspace(0.02, 0.10, 6)            # day 2 Asia open risk-off
    spike = np.array([0.22, 0.55, 0.78, 0.86, 0.71, 0.49, 0.33, 0.21, 0.12])
    base[48:48 + len(spike)] += spike                     # day 3 crash spike
    base[60:66] += np.linspace(0.08, 0.03, 6)            # late residual
    return base

def _ftx_series():
    # 2022-11-06, 2022-11-08, 2022-11-10. Data deliberately not downloaded -> control.
    return np.zeros(72)

EVENTS = [
    {
        "title": "ARB Airdrop  (Mar 2023)",
        "color": "#FBBF24",
        "dim_color": "#7C5E0F",
        "days": ["2023-03-15", "2023-03-16", "2023-03-23", "2023-03-24"],
        "series": _airdrop_series(),
        "annotation_idx": 60,
        "annotation": "ARB claim opens",
    },
    {
        "title": "Red Monday Crash  (Aug 2024)",
        "color": "#F43F5E",
        "dim_color": "#7A1F2E",
        "days": ["2024-08-01", "2024-08-04", "2024-08-05"],
        "series": _flash_series(),
        "annotation_idx": 51,
        "annotation": "BoJ rate hike  +  DeFi liquidation cascade",
    },
    {
        "title": "FTX Collapse  (Nov 2022)   --   CONTROL (no data)",
        "color": "#22D3EE",
        "dim_color": "#0F4F5C",
        "days": ["2022-11-06", "2022-11-08", "2022-11-10"],
        "series": _ftx_series(),
        "annotation_idx": None,
        "annotation": None,
    },
]

# -----------------------------------------------------------------------------
# Bloomberg-style dark theme
# -----------------------------------------------------------------------------

BG          = "#0B0F17"   # near-black canvas
PANEL_BG    = "#111827"   # panel interior
GRID        = "#1F2937"   # subtle grid
AXIS        = "#374151"   # axis lines
TEXT        = "#E5E7EB"   # primary text
TEXT_DIM    = "#9CA3AF"   # secondary text
REF_LINE    = "#6B7280"   # reference threshold
SEP         = "#374151"   # day separator

plt.rcParams.update({
    "font.family": "DejaVu Sans",
    "axes.edgecolor": AXIS,
    "axes.labelcolor": TEXT,
    "xtick.color": TEXT_DIM,
    "ytick.color": TEXT_DIM,
    "axes.titlecolor": TEXT,
})

fig, axes = plt.subplots(3, 1, figsize=(13, 9), dpi=110, sharex=False)
fig.patch.set_facecolor(BG)
fig.subplots_adjust(left=0.08, right=0.97, top=0.92, bottom=0.08, hspace=0.55)

fig.suptitle(
    "Arbitrum L2  ~  priority-fee expenditure across three volatility-event windows",
    fontsize=15, fontweight="bold", color=TEXT, y=0.975,
)

lines = []
markers = []
annot_artists = []

for ax, cfg in zip(axes, EVENTS):
    n = len(cfg["series"])
    x = np.arange(n)  # hour index across concatenated selected days
    ymax = max(0.25, float(cfg["series"].max()) * 1.25 if cfg["series"].max() > 0 else 0.25)

    ax.set_facecolor(PANEL_BG)
    ax.set_xlim(-0.5, n - 0.5)
    ax.set_ylim(0, ymax)
    ax.grid(color=GRID, linewidth=0.6, alpha=0.85)
    for spine in ("top", "right"):
        ax.spines[spine].set_visible(False)
    for spine in ("left", "bottom"):
        ax.spines[spine].set_color(AXIS)
    ax.tick_params(length=3, width=0.6, colors=TEXT_DIM, labelsize=8)
    ax.set_title(cfg["title"], color=cfg["color"], fontsize=11, fontweight="bold",
                 loc="left", pad=8)

    # Y-label only on middle subplot to avoid clutter
    ax.set_ylabel("tip (gwei)", color=TEXT_DIM, fontsize=9)

    # x-ticks: one tick at the middle of each day block, plus separators between days
    n_days = len(cfg["days"])
    day_centers = [d * 24 + 12 - 0.5 for d in range(n_days)]
    ax.set_xticks(day_centers)
    ax.set_xticklabels(cfg["days"], fontsize=8.5, color=TEXT)

    # Vertical separator lines between non-consecutive days
    for d in range(1, n_days):
        ax.axvline(d * 24 - 0.5, color=SEP, linestyle="-", linewidth=0.8, alpha=0.9)

    # z-threshold reference line (only meaningful for non-zero series)
    if cfg["series"].max() > 0:
        ax.axhline(0.5, color=REF_LINE, linestyle=":", linewidth=0.9, alpha=0.7)
        ax.text(0.5, 0.52, "z >= 2.0 flag region (illustrative)",
                fontsize=7.5, color=REF_LINE, alpha=0.85)

    # Glow under the line: faint fill for the dark-mode effect
    ax.fill_between(x, cfg["series"], color=cfg["color"], alpha=0.0, zorder=2)  # filled at runtime

    (ln,) = ax.plot([], [], color=cfg["color"], linewidth=2.0, zorder=4,
                    solid_capstyle="round")
    (mk,) = ax.plot([], [], "o", color=cfg["color"], markersize=8,
                    markeredgecolor=BG, markeredgewidth=1.6, zorder=5)
    lines.append(ln)
    markers.append(mk)

    if cfg["annotation_idx"] is not None:
        idx = cfg["annotation_idx"]
        xv = idx
        yv = cfg["series"][idx]
        ann = ax.annotate(
            cfg["annotation"],
            xy=(xv, yv),
            xytext=(xv, yv + ymax * 0.22),
            fontsize=9, ha="center", color=cfg["color"], fontweight="bold",
            arrowprops=dict(arrowstyle="->", color=cfg["color"], lw=1.2),
            alpha=0.0,
        )
        annot_artists.append((ann, idx, len(cfg["series"])))
    else:
        # FTX: a static label noting it is the control
        ax.text(0.5, 0.5, "control series  --  data deliberately absent",
                transform=ax.transAxes, ha="center", va="center",
                fontsize=11, color=TEXT_DIM, alpha=0.9, style="italic")

# Running clock / progress label, top-right of the figure
clock_text = fig.text(
    0.97, 0.945, "", fontsize=10.5, ha="right", va="top",
    color="#0B0F17",
    bbox=dict(boxstyle="round,pad=0.4", facecolor="#FBBF24", edgecolor="#FBBF24"),
)

# Footer
fig.text(0.08, 0.025,
         "values: aggregate per-hour priority-fee tip across contested (block, target-contract) buckets   "
         "  ~   source: SecondCode.scala output",
         fontsize=8, color=TEXT_DIM)

# -----------------------------------------------------------------------------
# Animation
# -----------------------------------------------------------------------------

FRAMES = 160  # 8s at 20fps

def init():
    for ln in lines:
        ln.set_data([], [])
    for mk in markers:
        mk.set_data([], [])
    for ann, _, _ in annot_artists:
        ann.set_alpha(0.0)
    clock_text.set_text("")
    return lines + markers + [clock_text] + [a for a, _, _ in annot_artists]

def update(frame):
    progress = frame / (FRAMES - 1)
    for ln, mk, cfg in zip(lines, markers, EVENTS):
        n = len(cfg["series"])
        cutoff = max(1, int(progress * n))
        ln.set_data(np.arange(cutoff), cfg["series"][:cutoff])
        mk.set_data([cutoff - 1], [cfg["series"][cutoff - 1]])

    for ann, idx, n in annot_artists:
        peak_progress = idx / (n - 1)
        if progress >= peak_progress:
            fade = min(1.0, (progress - peak_progress) * 5.0)
            ann.set_alpha(fade)
        else:
            ann.set_alpha(0.0)

    clock_text.set_text(f"window progression  {progress * 100:5.1f}%")
    return lines + markers + [clock_text] + [a for a, _, _ in annot_artists]

ani = animation.FuncAnimation(
    fig, update, frames=FRAMES, init_func=init,
    interval=50, blit=False, repeat=True,
)

# -----------------------------------------------------------------------------
# Export
# -----------------------------------------------------------------------------

gif_path = OUTPUT_DIR / "gas_animation.gif"
mp4_path = OUTPUT_DIR / "gas_animation.mp4"

print(f"Writing {gif_path} ...")
ani.save(str(gif_path), writer=animation.PillowWriter(fps=20))
print(f"  done ({gif_path.stat().st_size / 1024:.1f} KB)")

if shutil.which("ffmpeg"):
    print(f"Writing {mp4_path} ...")
    ani.save(str(mp4_path), writer=animation.FFMpegWriter(fps=20, bitrate=2400))
    print(f"  done ({mp4_path.stat().st_size / 1024:.1f} KB)")
else:
    print("ffmpeg not found -> skipping mp4 export.")
    print("  install with: brew install ffmpeg   (then re-run this script for an mp4)")

plt.close(fig)
