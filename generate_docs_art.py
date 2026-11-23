import math
import os
from PIL import Image, ImageDraw, ImageFont

REPO = r"c:\Users\raiyax\Music\12\SkyCasino"
TEX = os.path.join(REPO, "src", "main", "resources", "assets", "skycasino", "textures")
DOCS = os.path.join(REPO, "docs")
os.makedirs(DOCS, exist_ok=True)

def load_tex(rel, size):
    img = Image.open(os.path.join(TEX, rel)).convert("RGBA")
    return img.resize((size, size), Image.NEAREST)

def get_font(size, bold=False):
    candidates = [
        r"C:\Windows\Fonts\segoeuib.ttf" if bold else r"C:\Windows\Fonts\segoeui.ttf",
        r"C:\Windows\Fonts\arialbd.ttf" if bold else r"C:\Windows\Fonts\arial.ttf",
    ]
    for path in candidates:
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()

def make_banner():
    W, H = 1280, 400
    img = Image.new("RGBA", (W, H), (12, 8, 22, 255))
    d = ImageDraw.Draw(img)

    # Rich luxury night-casino vertical gradient
    for y in range(H):
        t = y / H
        r = int(18 * (1 - t) + 8 * t)
        g = int(8 * (1 - t) + 26 * t)
        b = int(28 * (1 - t) + 22 * t)
        d.line([(0, y), (W, y)], fill=(r, g, b, 255))

    # Subtle golden radial glow in center
    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for rad in range(280, 10, -10):
        alpha = int(14 * (1 - rad / 280))
        gd.ellipse([W//2 - rad*2, H//2 - rad, W//2 + rad*2, H//2 + rad], fill=(255, 200, 60, alpha))
    img = Image.alpha_composite(img, glow)
    d = ImageDraw.Draw(img)

    # Stars
    for i in range(70):
        sx = (i * 197 + 43) % (W - 40) + 20
        sy = (i * 89 + 29) % (H - 40) + 20
        sz = 2 if i % 4 == 0 else 1
        d.rectangle([sx, sy, sx + sz, sy + sz], fill=(255, 240, 185, 160))

    # Double luxury gold border
    d.rectangle([14, 14, W - 15, H - 15], outline=(170, 120, 32, 255), width=2)
    d.rectangle([22, 22, W - 23, H - 23], outline=(255, 212, 84, 255), width=2)
    for cx, cy in [(22, 22), (W - 30, 22), (22, H - 30), (W - 30, H - 30)]:
        d.rectangle([cx, cy, cx + 8, cy + 8], fill=(255, 220, 95, 255))

    # Left & Right feature textures (Slot Machine, Fortune Wheel, Crash, Mines)
    left_blocks = [
        ("block/slot_machine_front.png", 68, 72, 112),
        ("block/fortune_wheel_face.png", 196, 152, 96),
        ("item/gold_chip.png", 86, 214, 72),
        ("item/pocket_casino.png", 174, 266, 68),
    ]
    right_blocks = [
        ("block/crash_machine_front.png", W - 180, 72, 112),
        ("block/mines_machine_front.png", W - 292, 152, 96),
        ("item/loot_case_vip.png", W - 158, 214, 72),
        ("item/vip_card.png", W - 242, 266, 68),
    ]
    for rel, x, y, sz in left_blocks + right_blocks:
        t = load_tex(rel, sz)
        # Drop shadow
        d.rectangle([x + 6, y + 6, x + sz + 6, y + sz + 6], fill=(0, 0, 0, 90))
        img.alpha_composite(t, (x, y))
        d.rectangle([x - 2, y - 2, x + sz + 1, y + sz + 1], outline=(255, 209, 92, 140), width=2)

    # Center Title Typography
    f_tag = get_font(20, bold=True)
    f_title = get_font(78, bold=True)
    f_sub = get_font(24, bold=False)
    f_pills = get_font(17, bold=True)

    tag = "★  MINECRAFT 1.20.1 FORGE  ·  ATM9: TO THE SKY  ★"
    tb = d.textbbox((0, 0), tag, font=f_tag)
    d.text(((W - (tb[2] - tb[0])) // 2, 66), tag, font=f_tag, fill=(255, 209, 92, 235))

    title = "SKY CASINO"
    ttb = d.textbbox((0, 0), title, font=f_title)
    tx = (W - (ttb[2] - ttb[0])) // 2
    d.text((tx + 4, 108), title, font=f_title, fill=(0, 0, 0, 180))
    d.text((tx, 104), title, font=f_title, fill=(255, 224, 108, 255))

    sub = "6 игр · Портативный планшет · VIP-карта · 50+ модовых призов · Лудик"
    sb = d.textbbox((0, 0), sub, font=f_sub)
    d.text(((W - (sb[2] - sb[0])) // 2, 204), sub, font=f_sub, fill=(230, 232, 240, 240))

    # Row of 7 chips centered at bottom
    chips = [
        "item/white_chip.png", "item/red_chip.png", "item/blue_chip.png",
        "item/purple_chip.png", "item/gold_chip.png", "item/emerald_chip.png", "item/netherite_chip.png"
    ]
    chip_sz = 52
    gap = 18
    total_w = len(chips) * chip_sz + (len(chips) - 1) * gap
    start_x = (W - total_w) // 2
    for i, c in enumerate(chips):
        cx = start_x + i * (chip_sz + gap)
        img.alpha_composite(load_tex(c, chip_sz), (cx, 274))

    badge = "v1.5.0  ·  ALL-IN  ·  ЛОМБАРД  ·  КОЛЛЕКТОРЫ  ·  CS:GO КЕЙСЫ"
    bb = d.textbbox((0, 0), badge, font=f_pills)
    d.text(((W - (bb[2] - bb[0])) // 2, 344), badge, font=f_pills, fill=(130, 240, 185, 235))

    img.save(os.path.join(DOCS, "banner.png"))

def make_showcase():
    W, H = 1200, 310
    img = Image.new("RGBA", (W, H), (14, 13, 22, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 2, W - 3, H - 3], outline=(218, 168, 52, 255), width=2)

    f_head = get_font(20, bold=True)
    f_lbl = get_font(14, bold=False)

    d.text((24, 16), "ИГРОВЫЕ АВТОМАТЫ, СТОЛЫ И УНИКАЛЬНЫЕ ПРЕДМЕТЫ", font=f_head, fill=(255, 212, 88, 255))

    row1 = [
        ("block/slot_machine_front.png", "Слоты 777"),
        ("block/blackjack_top.png", "Блэкджек 21"),
        ("block/roulette_top.png", "Рулетка"),
        ("block/fortune_wheel_face.png", "Колесо Фортуны"),
        ("block/crash_machine_front.png", "Краш (Ракета)"),
        ("block/mines_machine_front.png", "Мины 5×5"),
        ("block/cashier_front.png", "Кассир"),
        ("block/prize_shop_front.png", "Магазин призов"),
    ]
    row2 = [
        ("item/pocket_casino.png", "Планшет казино"),
        ("item/vip_card.png", "VIP-карта"),
        ("item/loot_case_classic.png", "Обычный кейс"),
        ("item/loot_case_vip.png", "VIP-кейс"),
        ("item/anti_tilt_coffee.png", "Кофе Анти-Тильт"),
        ("block/casino_carpet_red.png", "Рубин. ковёр"),
        ("block/black_gold_marble.png", "Чёрный мрамор"),
        ("block/neon_pink.png", "Неон-блоки"),
    ]

    for r_idx, row in enumerate([row1, row2]):
        y = 54 + r_idx * 124
        for c_idx, (tex_path, label) in enumerate(row):
            x = 24 + c_idx * 146
            d.rounded_rectangle([x, y, x + 132, y + 110], radius=10, fill=(24, 22, 36, 255), outline=(95, 76, 38, 255), width=1)
            t = load_tex(tex_path, 64)
            img.alpha_composite(t, (x + 34, y + 12))
            lb = d.textbbox((0, 0), label, font=f_lbl)
            d.text((x + (132 - (lb[2] - lb[0])) // 2, y + 84), label, font=f_lbl, fill=(230, 232, 240, 255))

    img.save(os.path.join(DOCS, "showcase.png"))

if __name__ == "__main__":
    make_banner()
    make_showcase()
    print("Generated docs/banner.png and docs/showcase.png!")
