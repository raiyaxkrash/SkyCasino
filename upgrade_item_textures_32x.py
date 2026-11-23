import math
import os
from PIL import Image, ImageDraw

ITEM_DIR = r"c:\Users\raiyax\Music\12\SkyCasino\src\main\resources\assets\skycasino\textures\item"
os.makedirs(ITEM_DIR, exist_ok=True)

def clamp(v):
    return max(0, min(255, int(round(v))))

def shade_rgb(rgb, factor):
    return (clamp(rgb[0] * factor), clamp(rgb[1] * factor), clamp(rgb[2] * factor))

def save_item(name, img):
    img.save(os.path.join(ITEM_DIR, f"{name}.png"))

# ==========================================================
# 1. ULTRA-DETAILED 32x32 LUXURY CASINO CHIPS
# ==========================================================
def draw_luxury_chip(main_c, stripe_c, ring_c, center_c, emblem_c, emblem_type):
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    px = img.load()
    cx, cy = 15.5, 16.0  # slight vertical offset for 3D coin thickness

    # 3D bottom coin rim (thickness shadow)
    for y in range(32):
        for x in range(32):
            d_bot = math.hypot((x - cx) / 13.5, (y - (cy + 2.0)) / 12.2)
            if d_bot <= 1.0:
                if d_bot >= 0.90:
                    px[x, y] = (*shade_rgb(main_c, 0.30), 255)
                else:
                    ang = (math.degrees(math.atan2(y - (cy + 2.0), x - cx)) + 360) % 60
                    base = stripe_c if ang < 20 else main_c
                    px[x, y] = (*shade_rgb(base, 0.55), 255)

    # Top beveled face of chip
    for y in range(32):
        for x in range(32):
            dx = (x - cx) / 13.5
            dy = (y - (cy - 0.8)) / 12.2
            d = math.hypot(dx, dy)
            if d <= 1.0:
                # Directional lighting (top-left bright, bottom-right shaded)
                light = 1.0 + (-dx * 0.18 - dy * 0.22)
                if d >= 0.93:
                    # Crisp dark metallic outline
                    px[x, y] = (*shade_rgb(main_c, 0.38), 255)
                elif d >= 0.72:
                    # Outer clay rim with 6 radial stripes
                    ang = (math.degrees(math.atan2(dy, dx)) + 360 + 15) % 60
                    base = stripe_c if ang < 18 else main_c
                    # Subtle inner bevel highlight at rim transition
                    if 0.86 <= d < 0.93:
                        light += 0.14
                    px[x, y] = (*shade_rgb(base, light), 255)
                elif d >= 0.63:
                    # Recessed metallic gold/accent ring
                    px[x, y] = (*shade_rgb(ring_c, light * 1.06), 255)
                elif d >= 0.57:
                    # Inner groove shadow
                    px[x, y] = (*shade_rgb(center_c, 0.58), 255)
                else:
                    # Glossy center medallion
                    gloss = light + (0.22 if (dx + dy < -0.25) else 0.0)
                    px[x, y] = (*shade_rgb(center_c, gloss), 255)

    # Embossed Center Emblem (crisp pixel art per tier!)
    d = ImageDraw.Draw(img)
    ec = (*emblem_c, 255)
    ec_hi = (*shade_rgb(emblem_c, 1.35), 255)
    ec_sh = (*shade_rgb(emblem_c, 0.55), 255)

    if emblem_type == "dot":  # White (1) - classic diamond pip
        pts = [(15, 11), (16, 11), (14, 12), (17, 12), (13, 13), (18, 13),
               (12, 14), (19, 14), (12, 15), (19, 15), (13, 16), (18, 16),
               (14, 17), (17, 17), (15, 18), (16, 18)]
        d.polygon([(15, 10), (20, 15), (15, 20), (10, 15)], fill=ec, outline=ec_sh)
        d.polygon([(15, 12), (18, 15), (15, 18), (12, 15)], fill=ec_hi)
    elif emblem_type == "heart":  # Red (5)
        d.rectangle([12, 12, 14, 15], fill=ec)
        d.rectangle([17, 12, 19, 15], fill=ec)
        d.polygon([(11, 14), (20, 14), (15, 20)], fill=ec)
        d.point((13, 13), fill=ec_hi)
    elif emblem_type == "spade":  # Blue (25)
        d.polygon([(15, 10), (20, 16), (11, 16)], fill=ec)
        d.rectangle([12, 15, 14, 17], fill=ec)
        d.rectangle([17, 15, 19, 17], fill=ec)
        d.rectangle([15, 17, 16, 20], fill=ec)
        d.rectangle([13, 19, 18, 20], fill=ec_hi)
    elif emblem_type == "crown":  # Purple (100)
        d.rectangle([11, 16, 20, 19], fill=ec, outline=ec_sh)
        d.polygon([(11, 16), (11, 12), (14, 15), (15, 11), (17, 15), (20, 12), (20, 16)], fill=ec_hi)
        d.point((15, 17), fill=(255, 80, 160, 255))
    elif emblem_type == "star":  # Gold (1,000)
        d.polygon([(15, 9), (17, 13), (21, 14), (18, 17), (19, 21), (15, 18), (11, 21), (12, 17), (9, 14), (13, 13)], fill=ec_hi, outline=ec_sh)
        d.rectangle([14, 14, 16, 16], fill=(255, 255, 240, 255))
    elif emblem_type == "emerald":  # Emerald Mega (10,000)
        d.polygon([(13, 10), (18, 10), (20, 13), (20, 17), (18, 20), (13, 20), (11, 17), (11, 13)], fill=ec, outline=ec_sh)
        d.polygon([(14, 12), (17, 12), (18, 14), (18, 16), (17, 18), (14, 18), (13, 16), (13, 14)], fill=ec_hi)
        d.point((14, 13), fill=(255, 255, 255, 255))
    elif emblem_type == "mythic":  # Netherite/Star Mega (100,000)
        # Radiant 4-point mythic star + sparkles
        d.polygon([(15, 8), (17, 13), (22, 15), (17, 17), (15, 22), (13, 17), (8, 15), (13, 13)], fill=ec_hi, outline=ec_sh)
        d.rectangle([14, 14, 16, 16], fill=(255, 255, 255, 255))
        for sp in [(10, 10), (21, 10), (10, 20), (21, 20)]:
            px[sp[0], sp[1]] = (255, 240, 140, 255)

    return img

# ==========================================================
# 2. POCKET CASINO TABLET (32x32 Luxury Gaming Deck)
# ==========================================================
def draw_pocket_casino():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Drop shadow
    d.rounded_rectangle([6, 4, 27, 30], radius=3, fill=(10, 8, 16, 160))
    # Outer 24k Gold Metallic Casing
    d.rounded_rectangle([5, 3, 26, 29], radius=3, fill=(225, 172, 42, 255), outline=(120, 82, 16, 255))
    # Top-left gold specular bevel
    d.line([7, 4, 24, 4], fill=(255, 238, 155, 255))
    d.line([6, 5, 6, 27], fill=(250, 214, 98, 255))
    # Dark obsidian inner bezel
    d.rounded_rectangle([7, 5, 24, 27], radius=2, fill=(24, 20, 34, 255))

    # Glowing Emerald-Neon Screen
    d.rectangle([8, 6, 23, 20], fill=(12, 48, 38, 255), outline=(185, 85, 255, 255))
    d.rectangle([9, 7, 22, 19], fill=(16, 66, 52, 255))
    # Mini 3-Reel Slot Display on screen
    for rx in [10, 14, 18]:
        d.rectangle([rx, 9, rx + 3, 17], fill=(248, 244, 232, 255), outline=(218, 168, 45, 255))
        # Tiny red/gold "7" in each reel
        d.line([rx + 1, 11, rx + 2, 11], fill=(220, 28, 38, 255))
        d.point((rx + 2, 12), fill=(220, 28, 38, 255))
        d.point((rx + 1, 13), fill=(220, 28, 38, 255))
        d.point((rx + 1, 14), fill=(220, 28, 38, 255))
    # Screen glass diagonal reflection
    d.line([9, 8, 12, 7], fill=(150, 255, 225, 140))

    # Glowing Arcade Buttons below screen
    d.rectangle([9, 22, 12, 25], fill=(240, 55, 75, 255), outline=(120, 18, 28, 255))
    d.point((10, 23), fill=(255, 165, 175, 255))
    d.rectangle([14, 22, 17, 25], fill=(45, 225, 125, 255), outline=(16, 105, 52, 255))
    d.point((15, 23), fill=(175, 255, 205, 255))
    d.rectangle([19, 22, 22, 25], fill=(55, 195, 255, 255), outline=(18, 88, 135, 255))
    d.point((20, 23), fill=(185, 242, 255, 255))

    # Top glowing amethyst crystal emitter
    d.rectangle([14, 1, 17, 3], fill=(215, 105, 255, 255), outline=(120, 35, 165, 255))
    d.point((15, 2), fill=(255, 225, 255, 255))

    return img

# ==========================================================
# 3. VIP CASINO CARD (32x32 Obsidian & 24k Gold Metal Card)
# ==========================================================
def draw_vip_card():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Drop shadow
    d.rounded_rectangle([3, 8, 30, 26], radius=3, fill=(0, 0, 0, 120))
    # Outer Gold Metal Rim
    d.rounded_rectangle([2, 7, 29, 25], radius=3, fill=(235, 182, 46, 255), outline=(115, 78, 16, 255))
    # Obsidian Carbon Face
    d.rounded_rectangle([4, 9, 27, 23], radius=2, fill=(22, 20, 32, 255))

    # Metallic diagonal sheen across card
    for i in range(14):
        x0 = 6 + i
        y0 = 22 - i
        if 4 <= x0 <= 27 and 9 <= y0 <= 23:
            d.point((x0, y0), fill=(48, 44, 68, 255))
            if x0 + 1 <= 27:
                d.point((x0 + 1, y0), fill=(36, 33, 52, 255))

    # Top Golden Stripe
    d.rectangle([4, 10, 27, 12], fill=(225, 174, 42, 255))
    d.line([4, 10, 27, 10], fill=(255, 236, 145, 255))

    # Embossed Gold EMV Chip (left side)
    d.rectangle([6, 15, 11, 19], fill=(248, 208, 78, 255), outline=(145, 102, 22, 255))
    d.line([8, 15, 8, 19], fill=(165, 120, 28, 255))
    d.line([6, 17, 11, 17], fill=(165, 120, 28, 255))

    # Glowing Amethyst/Gold VIP Crown Emblem (right side)
    d.rectangle([18, 19, 25, 21], fill=(245, 195, 55, 255))
    d.polygon([(18, 19), (18, 15), (20, 17), (21, 14), (23, 17), (25, 15), (25, 19)], fill=(255, 230, 110, 255))
    d.point((21, 19), fill=(215, 75, 255, 255))
    d.point((22, 19), fill=(215, 75, 255, 255))

    # Subtle sparkle on top-right
    d.point((26, 6), fill=(255, 245, 175, 255))
    d.point((27, 5), fill=(255, 225, 95, 200))
    return img

# ==========================================================
# 4. CLASSIC & VIP LOOT CASES (32x32 Detailed 3D Briefcases)
# ==========================================================
def draw_loot_case(vip):
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    if vip:
        body = (54, 18, 72)
        lid = (82, 26, 108)
        trim = (242, 190, 48)
        trim_hi = (255, 240, 155)
        trim_sh = (135, 92, 18)
        glow = (255, 75, 205)
        glow_hi = (255, 205, 245)
    else:
        body = (28, 52, 86)
        lid = (42, 78, 124)
        trim = (185, 205, 225)
        trim_hi = (235, 248, 255)
        trim_sh = (65, 85, 112)
        glow = (45, 215, 248)
        glow_hi = (200, 252, 255)

    # Carry Handle on top
    d.rectangle([11, 5, 20, 7], fill=(*trim, 255), outline=(*trim_sh, 255))
    d.rectangle([11, 7, 13, 10], fill=(*trim, 255))
    d.rectangle([18, 7, 20, 10], fill=(*trim, 255))
    d.line([12, 6, 19, 6], fill=(*trim_hi, 255))

    # Main Case Body
    d.rounded_rectangle([3, 9, 28, 27], radius=3, fill=(*body, 255), outline=(*trim_sh, 255))
    # Top Lid Bevel
    d.rounded_rectangle([4, 10, 27, 17], radius=2, fill=(*lid, 255))
    d.line([5, 10, 26, 10], fill=(*trim_hi, 255))

    # Reinforced Metallic Corner Brackets
    for bx, by in [(3, 9), (24, 9), (3, 23), (24, 23)]:
        d.rectangle([bx, by, bx + 4, by + 4], fill=(*trim, 255), outline=(*trim_sh, 255))
        d.point((bx + 1, by + 1), fill=(*trim_hi, 255))

    # Horizontal Seam + Glowing Energy Strip
    d.rectangle([4, 16, 27, 18], fill=(*trim_sh, 255))
    d.line([6, 17, 25, 17], fill=(*glow, 255))
    d.line([8, 17, 23, 17], fill=(*glow_hi, 255))

    # Central Golden/Jewel Lock Clasp
    d.rectangle([13, 14, 18, 21], fill=(242, 192, 48, 255), outline=(128, 86, 16, 255))
    d.rectangle([14, 16, 17, 19], fill=(*glow, 255))
    d.point((15, 17), fill=(255, 255, 255, 255))

    # Vertical ribs on lower body
    for rx in [9, 22]:
        d.line([rx, 19, rx, 25], fill=(*shade_rgb(body, 0.65), 255))
        d.line([rx + 1, 19, rx + 1, 25], fill=(*shade_rgb(body, 1.25), 255))

    # Sparkles around VIP Case
    if vip:
        for sx, sy in [(6, 5), (25, 6), (2, 18), (29, 14)]:
            d.point((sx, sy), fill=(255, 240, 145, 255))
            d.point((sx + 1, sy), fill=(255, 210, 75, 180))

    return img

# ==========================================================
# 5. ANTI-TILT ESPRESSO MUG (32x32 Steaming Luxury Coffee)
# ==========================================================
def draw_anti_tilt_coffee():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Rising aromatic cyan-white steam curls
    steam1 = [(11, 8), (10, 7), (11, 6), (12, 5), (11, 4)]
    steam2 = [(16, 7), (17, 6), (16, 5), (15, 4), (16, 3)]
    steam3 = [(20, 8), (21, 7), (20, 6), (19, 5)]
    for i, p in enumerate(steam1 + steam2 + steam3):
        a = 220 - (i % 5) * 30
        d.point(p, fill=(205, 248, 255, a))

    # Golden Mug Handle (right side)
    d.rounded_rectangle([21, 13, 27, 23], radius=3, outline=(235, 182, 46, 255), width=2)
    d.line([23, 14, 25, 14], fill=(255, 238, 155, 255))

    # Saucer / Base shadow
    d.ellipse([5, 25, 24, 29], fill=(215, 165, 38, 255), outline=(115, 78, 16, 255))

    # Mug Cylinder Body (rich obsidian-espresso ceramic with vertical lighting)
    for y in range(11, 27):
        for x in range(7, 22):
            nx = (x - 14.0) / 7.5
            light = 1.0 - nx * 0.35 + (0.25 if -0.65 < nx < -0.25 else 0.0)
            col = shade_rgb((46, 36, 58), light)
            img.putpixel((x, y), (*col, 255))

    # Gold Rim & Bottom Trim
    d.rectangle([7, 11, 22, 13], fill=(242, 192, 48, 255), outline=(128, 86, 18, 255))
    d.line([8, 12, 21, 12], fill=(255, 242, 165, 255))
    d.line([7, 26, 22, 26], fill=(235, 182, 46, 255))

    # Rich Coffee Crema inside top opening
    d.ellipse([8, 10, 21, 14], fill=(118, 66, 32, 255), outline=(235, 185, 50, 255))
    d.ellipse([10, 11, 18, 13], fill=(168, 108, 56, 255))

    # Glowing Cyan Anti-Tilt Diamond Emblem on mug front
    d.polygon([(14, 16), (18, 20), (14, 24), (10, 20)], fill=(45, 220, 250, 255), outline=(18, 110, 145, 255))
    d.polygon([(14, 18), (16, 20), (14, 22), (12, 20)], fill=(195, 252, 255, 255))
    d.point((14, 19), fill=(255, 255, 255, 255))

    return img

def main():
    # 7 Luxury 32x32 Chips
    save_item("white_chip",     draw_luxury_chip((232, 236, 244), (58, 72, 98),   (185, 198, 218), (248, 250, 255), (70, 88, 120),   "dot"))
    save_item("red_chip",       draw_luxury_chip((205, 32, 46),   (248, 242, 232),(245, 202, 78),  (228, 52, 66),   (255, 225, 120), "heart"))
    save_item("blue_chip",      draw_luxury_chip((26, 108, 224),  (240, 248, 255),(110, 225, 255), (48, 142, 245),  (225, 250, 255), "spade"))
    save_item("purple_chip",    draw_luxury_chip((142, 36, 205),  (255, 212, 78), (255, 218, 92),  (168, 58, 232),  (255, 220, 88),  "crown"))
    save_item("gold_chip",      draw_luxury_chip((238, 176, 24),  (255, 248, 195),(255, 238, 140), (252, 204, 58),  (255, 250, 205), "star"))
    save_item("emerald_chip",   draw_luxury_chip((18, 168, 86),   (255, 222, 82), (248, 212, 85),  (38, 208, 114),  (205, 255, 225), "emerald"))
    save_item("netherite_chip", draw_luxury_chip((42, 26, 58),    (255, 198, 48), (255, 212, 78),  (88, 34, 132),   (255, 232, 115), "mythic"))

    # 5 Luxury 32x32 Special Items
    save_item("pocket_casino",     draw_pocket_casino())
    save_item("vip_card",          draw_vip_card())
    save_item("loot_case_classic", draw_loot_case(False))
    save_item("loot_case_vip",     draw_loot_case(True))
    save_item("anti_tilt_coffee",  draw_anti_tilt_coffee())

    print("All 12 item textures upgraded to 32x32 HD pixel art!")

if __name__ == "__main__":
    main()
