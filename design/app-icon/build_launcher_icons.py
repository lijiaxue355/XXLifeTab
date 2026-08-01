from pathlib import Path

from PIL import Image, ImageDraw


PROJECT_ROOT = Path(__file__).resolve().parents[2]
FOREGROUND_PATH = (
    PROJECT_ROOT
    / "app"
    / "src"
    / "main"
    / "res"
    / "drawable-nodpi"
    / "lifelab_icon_foreground.png"
)
RES_DIR = PROJECT_ROOT / "app" / "src" / "main" / "res"
BACKGROUND = (232, 250, 247, 255)
SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def render_icon(foreground: Image.Image, size: int, round_icon: bool) -> Image.Image:
    icon = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    mask = Image.new("L", (size, size), 0)
    mask_draw = ImageDraw.Draw(mask)

    if round_icon:
        mask_draw.ellipse((0, 0, size - 1, size - 1), fill=255)
    else:
        radius = round(size * 0.22)
        mask_draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=radius, fill=255)

    background = Image.new("RGBA", (size, size), BACKGROUND)
    icon.paste(background, mask=mask)

    resized_foreground = foreground.resize((size, size), Image.Resampling.LANCZOS)
    icon.alpha_composite(resized_foreground)
    return icon


def main() -> None:
    foreground = Image.open(FOREGROUND_PATH).convert("RGBA")

    for density, size in SIZES.items():
        output_dir = RES_DIR / f"mipmap-{density}"
        output_dir.mkdir(parents=True, exist_ok=True)
        render_icon(foreground, size, round_icon=False).save(
            output_dir / "ic_launcher.webp",
            format="WEBP",
            lossless=True,
        )
        render_icon(foreground, size, round_icon=True).save(
            output_dir / "ic_launcher_round.webp",
            format="WEBP",
            lossless=True,
        )


if __name__ == "__main__":
    main()
