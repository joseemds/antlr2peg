#!/usr/bin/env python3
"""
Usage: python plot_grammar_results.py results.json [output.png]
"""

import json
import sys
from collections import Counter
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

PALETTE = {
    "SUCCESS":         "#4caf78",
    "MISSING_FILE":    "#e07b54",
    "MULTIPLE_FILES":  "#5b8dd9",
    "LEFT_RECURSION":  "#c9547c",
    "UNKNOWN":         "#9e9e9e",
}

def classify(entry):
    if "tracker" in entry:
        return "SUCCESS"
    kind = entry.get("kind", "")
    if kind in ("MISSING_FILE", "MULTIPLE_FILES", "LEFT_RECURSION"):
        return kind
    return "UNKNOWN"

json_file = sys.argv[1]
output    = sys.argv[2] if len(sys.argv) > 2 else "output.png"

with open(json_file) as f:
    data = json.load(f)

counts = Counter(classify(e) for e in data.values())

order  = ["SUCCESS", "MISSING_FILE", "MULTIPLE_FILES", "LEFT_RECURSION", "UNKNOWN"]
labels = [k for k in order if k in counts]
values = [counts[k] for k in labels]
colors = [PALETTE[k] for k in labels]
total  = sum(values)

fig, ax = plt.subplots(figsize=(9, 5))
fig.patch.set_facecolor("#1a1a2e")
ax.set_facecolor("#16213e")

bars = ax.bar(labels, values, color=colors, width=0.55, edgecolor="#ffffff22", linewidth=0.8)

for bar, val in zip(bars, values):
    ax.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 0.15,
            f"{val} ({val/total*100:.1f}%)",
            ha="center", va="bottom", color="white", fontsize=10, fontweight="bold")

ax.set_title("Grammar Analysis Results", color="white", fontsize=16, fontweight="bold", pad=16)
ax.set_ylabel("Count", color="#cccccc", fontsize=12)
ax.tick_params(colors="white", labelsize=11)
ax.set_xticklabels([l.replace("_", "\n") for l in labels], color="white")
for spine in ax.spines.values():
    spine.set_edgecolor("#333366")
ax.yaxis.grid(True, color="#ffffff18", linestyle="--", linewidth=0.7)
ax.set_axisbelow(True)
fig.text(0.98, 0.02, f"Total: {total}", ha="right", color="#888888", fontsize=9)

plt.tight_layout()
plt.savefig(output, dpi=150, bbox_inches="tight", facecolor=fig.get_facecolor())
print(f"Saved: {output}")
