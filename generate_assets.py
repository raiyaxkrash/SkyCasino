import json
import math
import os
from PIL import Image, ImageDraw

ROOT = r"c:\Users\raiyax\Music\12\SkyCasino\src\main\resources"
ASSETS = os.path.join(ROOT, "assets", "skycasino")
DATA = os.path.join(ROOT, "data", "skycasino")

def ensure_dir(path):
    os.makedirs(path, exist_ok=True)

def save_json(path, obj):
    ensure_dir(os.path.dirname(path))
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)

def save_png(rel_path, img):
    full = os.path.join(ASSETS, "textures", rel_path)
    ensure_dir(os.path.dirname(full))
    img.save(full)

# ==========================================
# 1. PROCEDURAL PIXEL-ART TEXTURE GENERATOR
# ==========================================
def draw_chip_texture(main_rgb, edge_rgb, center_rgb, accent_rgb):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cx, cy = 7.5, 7.5
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            if d <= 6.8:
                # Outer ring
                if d >= 5.2:
                    angle = math.degrees(math.atan2(y - cy, x - cx)) % 60
                    if angle < 22:
                        px[x, y] = (*edge_rgb, 255)
                    else:
                        px[x, y] = (*main_rgb, 255)
                elif d >= 4.2:
                    px[x, y] = (*accent_rgb, 255)
                else:
                    # Inner medallion with subtle shading
                    shade = 1.08 if (x + y < 14) else 0.92
                    r = min(255, int(center_rgb[0] * shade))
                    g = min(255, int(center_rgb[1] * shade))
                    b = min(255, int(center_rgb[2] * shade))
                    px[x, y] = (r, g, b, 255)
                # Subtle dark outline on outer rim
                if 6.1 <= d <= 6.8:
                    r, g, b, a = px[x, y]
                    px[x, y] = (int(r * 0.6), int(g * 0.6), int(b * 0.6), 255)
    # Center star/diamond sparkle
    for ox, oy in [(7, 7), (8, 8), (7, 8), (8, 7)]:
        px[ox, oy] = (*accent_rgb, 255)
    return img

def generate_textures():
    # Chips
    save_png("item/white_chip.png", draw_chip_texture((230, 232, 238), (60, 70, 90), (248, 248, 252), (180, 190, 205)))
    save_png("item/red_chip.png", draw_chip_texture((195, 32, 42), (245, 240, 235), (225, 55, 65), (250, 210, 95)))
    save_png("item/blue_chip.png", draw_chip_texture((28, 108, 215), (240, 248, 255), (52, 148, 240), (140, 235, 255)))
    save_png("item/purple_chip.png", draw_chip_texture((140, 40, 195), (255, 215, 90), (175, 72, 230), (255, 225, 120)))
    save_png("item/gold_chip.png", draw_chip_texture((235, 175, 25), (255, 248, 190), (255, 208, 65), (255, 255, 225)))

    # Carpet Red (Ruby + Gold luxury damask pattern)
    img = Image.new("RGBA", (16, 16), (138, 16, 28, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(218, 168, 42, 255))
    d.rectangle([2, 2, 13, 13], outline=(168, 24, 38, 255))
    for p in [(7, 4), (8, 4), (7, 11), (8, 11), (4, 7), (4, 8), (11, 7), (11, 8), (7, 7), (8, 8), (7, 8), (8, 7)]:
        img.putpixel(p, (238, 190, 62, 255))
    save_png("block/casino_carpet_red.png", img)

    # Carpet Green (Emerald + Gold luxury pattern)
    img = Image.new("RGBA", (16, 16), (14, 92, 52, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(218, 168, 42, 255))
    d.rectangle([2, 2, 13, 13], outline=(22, 122, 68, 255))
    for p in [(7, 4), (8, 4), (7, 11), (8, 11), (4, 7), (4, 8), (11, 7), (11, 8), (7, 7), (8, 8), (7, 8), (8, 7)]:
        img.putpixel(p, (238, 190, 62, 255))
    save_png("block/casino_carpet_green.png", img)

    # Gold Marble (White quartz with gold veins)
    img = Image.new("RGBA", (16, 16), (242, 239, 232, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(222, 215, 200, 255))
    veins = [(2, 1), (3, 2), (4, 2), (5, 3), (6, 4), (7, 4), (8, 5), (9, 6), (10, 7), (11, 8), (12, 9), (13, 11),
             (1, 10), (2, 10), (3, 11), (4, 12), (5, 12), (6, 13), (8, 14), (9, 14)]
    for vx, vy in veins:
        img.putpixel((vx, vy), (218, 174, 62, 255))
        if vx + 1 < 16:
            img.putpixel((vx + 1, vy), (235, 208, 125, 255))
    save_png("block/gold_marble.png", img)

    # Black Gold Marble (Obsidian-black with gold veins)
    img = Image.new("RGBA", (16, 16), (22, 22, 28, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(38, 36, 44, 255))
    for vx, vy in veins:
        img.putpixel((vx, vy), (232, 182, 52, 255))
        if vy + 1 < 16:
            img.putpixel((vx, vy + 1), (135, 100, 28, 255))
    save_png("block/black_gold_marble.png", img)

    # Neon blocks (dark metallic frame + glowing inner neon core)
    neons = {
        "neon_gold": ((255, 210, 60), (255, 245, 170)),
        "neon_pink": ((240, 55, 185), (255, 175, 235)),
        "neon_cyan": ((35, 225, 245), (180, 250, 255)),
        "neon_emerald": ((35, 230, 125), (175, 255, 210)),
    }
    for name, (outer_c, inner_c) in neons.items():
        img = Image.new("RGBA", (16, 16), (20, 20, 28, 255))
        d = ImageDraw.Draw(img)
        d.rectangle([1, 1, 14, 14], outline=(55, 55, 70, 255))
        d.rectangle([2, 2, 13, 13], fill=(*outer_c, 255))
        d.rectangle([4, 4, 11, 11], fill=(*inner_c, 255))
        save_png(f"block/{name}.png", img)

    # Casino Counter (Mahogany wood + gold trim)
    img = Image.new("RGBA", (16, 16), (88, 28, 22, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 2], fill=(228, 178, 48, 255))
    d.rectangle([0, 14, 15, 15], fill=(200, 150, 38, 255))
    d.rectangle([2, 4, 13, 12], outline=(210, 160, 44, 255))
    d.rectangle([3, 5, 12, 11], fill=(68, 20, 16, 255))
    save_png("block/casino_counter.png", img)

    # Casino Sign Block (Glowing dark marquee with golden diamond/star border)
    img = Image.new("RGBA", (16, 16), (28, 10, 22, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(255, 205, 55, 255))
    d.rectangle([2, 2, 13, 13], outline=(245, 65, 165, 255))
    # Big glowing "7" / star emblem
    for x in range(5, 11):
        img.putpixel((x, 4), (255, 235, 110, 255))
    for i, y in enumerate(range(5, 12)):
        x = max(5, 10 - i // 2)
        img.putpixel((x, y), (255, 225, 90, 255))
        if x - 1 >= 5:
            img.putpixel((x - 1, y), (255, 185, 40, 255))
    save_png("block/casino_sign_block.png", img)

    # Slot Machine Front (32x32 crisp pixel art!)
    img = Image.new("RGBA", (32, 32), (125, 18, 28, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 31, 31], outline=(232, 180, 45, 255), width=2)
    # Top neon header
    d.rectangle([3, 3, 28, 8], fill=(24, 12, 34, 255), outline=(255, 215, 75, 255))
    for x in range(6, 26, 4):
        d.rectangle([x, 5, x + 1, 6], fill=(255, 230, 90, 255))
    # 3 Reel Window
    d.rectangle([3, 10, 28, 22], fill=(245, 242, 232, 255), outline=(235, 185, 45, 255))
    d.line([11, 11, 11, 21], fill=(160, 140, 110, 255))
    d.line([20, 11, 20, 21], fill=(160, 140, 110, 255))
    # Three red/gold "7"s in reels
    for rx in [5, 14, 23]:
        d.line([rx, 13, rx + 4, 13], fill=(220, 25, 35, 255))
        d.line([rx + 4, 13, rx + 1, 19], fill=(220, 25, 35, 255))
        d.point((rx + 2, 16), fill=(255, 200, 40, 255))
    # Buttons & coin tray
    d.rectangle([5, 24, 10, 26], fill=(40, 215, 95, 255))
    d.rectangle([13, 24, 18, 26], fill=(245, 195, 40, 255))
    d.rectangle([21, 24, 26, 26], fill=(225, 45, 55, 255))
    d.rectangle([6, 28, 25, 30], fill=(30, 28, 35, 255))
    save_png("block/slot_machine_front.png", img)

    # Slot Machine Side / Cabinet
    img = Image.new("RGBA", (16, 16), (112, 16, 26, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(220, 170, 42, 255))
    d.rectangle([3, 3, 12, 12], fill=(85, 12, 20, 255), outline=(185, 135, 34, 255))
    save_png("block/slot_machine_side.png", img)

    # Gold Metal block texture (for machine frames, stool legs, chandelier)
    img = Image.new("RGBA", (16, 16), (235, 185, 48, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(182, 132, 26, 255))
    d.rectangle([2, 2, 13, 13], fill=(248, 206, 78, 255))
    d.line([3, 3, 12, 3], fill=(255, 240, 165, 255))
    save_png("block/casino_gold_trim.png", img)

    # Cashier Terminal Front (ATM / Chip Exchange screen)
    img = Image.new("RGBA", (16, 16), (32, 26, 38, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(235, 185, 48, 255))
    d.rectangle([2, 2, 13, 9], fill=(12, 68, 52, 255), outline=(85, 235, 165, 255))
    # Dollar/Chip icon on screen
    d.rectangle([6, 4, 9, 7], fill=(255, 220, 70, 255))
    # Keypad
    d.rectangle([3, 11, 12, 13], fill=(190, 150, 45, 255))
    save_png("block/cashier_front.png", img)

    # Blackjack Table Top (Green felt + gold arc + 2 mini cards)
    img = Image.new("RGBA", (32, 32), (14, 104, 56, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 31, 31], outline=(92, 32, 22, 255), width=2)
    d.rectangle([2, 2, 29, 29], outline=(230, 182, 48, 255))
    # Gold semicircle arc
    d.arc([6, 6, 25, 25], 0, 180, fill=(235, 195, 72, 255))
    # Two mini playing cards
    d.rectangle([10, 9, 14, 15], fill=(250, 248, 242, 255), outline=(40, 40, 40, 255))
    d.point((12, 12), fill=(210, 25, 35, 255))
    d.rectangle([17, 9, 21, 15], fill=(250, 248, 242, 255), outline=(40, 40, 40, 255))
    d.point((19, 12), fill=(20, 20, 25, 255))
    # Chip circles
    d.ellipse([13, 21, 18, 26], fill=(235, 185, 45, 255), outline=(255, 245, 175, 255))
    save_png("block/blackjack_top.png", img)

    # Roulette Table Top (32x32 green felt + circular roulette wheel + red/black grid)
    img = Image.new("RGBA", (32, 32), (14, 98, 52, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 31, 31], outline=(88, 28, 20, 255), width=2)
    d.rectangle([2, 2, 29, 29], outline=(228, 178, 48, 255))
    # Wheel on left half
    d.ellipse([4, 7, 17, 20], fill=(115, 42, 22, 255), outline=(240, 192, 55, 255))
    d.ellipse([6, 9, 15, 18], fill=(205, 28, 38, 255))
    d.ellipse([8, 11, 13, 16], fill=(24, 24, 30, 255))
    d.rectangle([10, 13, 11, 14], fill=(255, 220, 75, 255))
    # Betting grid on right half
    for r in range(5):
        for c in range(2):
            col = (200, 28, 38, 255) if (r + c) % 2 == 0 else (26, 26, 32, 255)
            d.rectangle([20 + c * 4, 6 + r * 4, 23 + c * 4, 9 + r * 4], fill=col, outline=(220, 180, 60, 255))
    save_png("block/roulette_top.png", img)

    # Fortune Wheel Face (32x32 colorful wheel!)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    px = img.load()
    colors = [
        (235, 185, 30), (205, 35, 45), (35, 165, 90), (45, 115, 220),
        (165, 50, 205), (235, 120, 30), (35, 195, 195), (215, 45, 145)
    ]
    cx, cy = 15.5, 15.5
    for y in range(32):
        for x in range(32):
            dist = math.hypot(x - cx, y - cy)
            if dist <= 14.8:
                if dist >= 13.0:
                    px[x, y] = (242, 192, 50, 255)
                elif dist <= 3.0:
                    px[x, y] = (255, 232, 110, 255)
                else:
                    ang = (math.degrees(math.atan2(y - cy, x - cx)) + 360) % 360
                    idx = int(ang // 45) % len(colors)
                    px[x, y] = (*colors[idx], 255)
    save_png("block/fortune_wheel_face.png", img)

    # Chandelier Crystal
    img = Image.new("RGBA", (16, 16), (225, 245, 255, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(175, 220, 255, 255))
    d.rectangle([3, 3, 12, 12], fill=(255, 255, 255, 255))
    save_png("block/casino_crystal.png", img)

    # Velvet Cushion (for stool)
    img = Image.new("RGBA", (16, 16), (165, 22, 36, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(120, 14, 24, 255))
    d.rectangle([4, 4, 11, 11], fill=(192, 32, 48, 255))
    save_png("block/casino_velvet.png", img)

# ==========================================
# 2. 3D BLOCK MODELS & BLOCKSTATES
# ==========================================
def generate_models_and_blockstates():
    # Simple full cube blocks
    simple_cubes = [
        "gold_marble", "black_gold_marble",
        "neon_gold", "neon_pink", "neon_cyan", "neon_emerald",
        "casino_counter", "casino_sign_block"
    ]
    for name in simple_cubes:
        save_json(os.path.join(ASSETS, "models", "block", f"{name}.json"), {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": f"skycasino:block/{name}"}
        })
        save_json(os.path.join(ASSETS, "blockstates", f"{name}.json"), {
            "variants": {"": {"model": f"skycasino:block/{name}"}}
        })
        save_json(os.path.join(ASSETS, "models", "item", f"{name}.json"), {
            "parent": f"skycasino:block/{name}"
        })

    # Carpets
    for name in ["casino_carpet_red", "casino_carpet_green"]:
        save_json(os.path.join(ASSETS, "models", "block", f"{name}.json"), {
            "parent": "minecraft:block/carpet",
            "textures": {"wool": f"skycasino:block/{name}"}
        })
        save_json(os.path.join(ASSETS, "blockstates", f"{name}.json"), {
            "variants": {"": {"model": f"skycasino:block/{name}"}}
        })
        save_json(os.path.join(ASSETS, "models", "item", f"{name}.json"), {
            "parent": f"skycasino:block/{name}"
        })

    # 3D Slot Machine Model (Cabinet + Marquee + Side Lever!)
    save_json(os.path.join(ASSETS, "models", "block", "slot_machine.json"), {
        "textures": {
            "front": "skycasino:block/slot_machine_front",
            "side": "skycasino:block/slot_machine_side",
            "gold": "skycasino:block/casino_gold_trim",
            "velvet": "skycasino:block/casino_velvet",
            "particle": "skycasino:block/slot_machine_front"
        },
        "elements": [
            # Main cabinet body
            {
                "from": [2, 0, 2], "to": [14, 14, 14],
                "faces": {
                    "north": {"texture": "#front", "uv": [0, 0, 16, 16]},
                    "south": {"texture": "#side"},
                    "east": {"texture": "#side"},
                    "west": {"texture": "#side"},
                    "up": {"texture": "#gold"},
                    "down": {"texture": "#side"}
                }
            },
            # Top glowing marquee crown
            {
                "from": [3, 14, 4], "to": [13, 16, 12],
                "faces": {
                    "north": {"texture": "#gold"},
                    "south": {"texture": "#gold"},
                    "east": {"texture": "#gold"},
                    "west": {"texture": "#gold"},
                    "up": {"texture": "#gold"}
                }
            },
            # Lever arm on right side
            {
                "from": [14, 6, 7], "to": [15.5, 7.5, 9],
                "faces": {
                    "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                    "east": {"texture": "#gold"}, "up": {"texture": "#gold"}, "down": {"texture": "#gold"}
                }
            },
            {
                "from": [14.5, 7.5, 7.5], "to": [15.5, 12, 8.5],
                "faces": {
                    "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                    "east": {"texture": "#gold"}, "west": {"texture": "#gold"}
                }
            },
            # Red lever knob
            {
                "from": [14, 12, 7], "to": [16, 14, 9],
                "faces": {
                    "north": {"texture": "#velvet"}, "south": {"texture": "#velvet"},
                    "east": {"texture": "#velvet"}, "west": {"texture": "#velvet"},
                    "up": {"texture": "#velvet"}, "down": {"texture": "#velvet"}
                }
            }
        ]
    })

    # 3D Cashier Terminal Model
    save_json(os.path.join(ASSETS, "models", "block", "cashier_terminal.json"), {
        "textures": {
            "front": "skycasino:block/cashier_front",
            "side": "skycasino:block/black_gold_marble",
            "gold": "skycasino:block/casino_gold_trim",
            "particle": "skycasino:block/cashier_front"
        },
        "elements": [
            {"from": [1, 0, 2], "to": [15, 2, 14], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                "up": {"texture": "#gold"}, "down": {"texture": "#side"}
            }},
            {"from": [2, 2, 3], "to": [14, 15, 13], "faces": {
                "north": {"texture": "#front"}, "south": {"texture": "#side"},
                "east": {"texture": "#side"}, "west": {"texture": "#side"},
                "up": {"texture": "#gold"}
            }},
            {"from": [3, 15, 4], "to": [13, 16, 12], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                "up": {"texture": "#gold"}
            }}
        ]
    })

    # 3D Blackjack Table Model
    save_json(os.path.join(ASSETS, "models", "block", "blackjack_table.json"), {
        "textures": {
            "top": "skycasino:block/blackjack_top",
            "wood": "skycasino:block/casino_counter",
            "gold": "skycasino:block/casino_gold_trim",
            "particle": "skycasino:block/blackjack_top"
        },
        "elements": [
            # Pedestal base
            {"from": [4, 0, 4], "to": [12, 2, 12], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                "up": {"texture": "#gold"}, "down": {"texture": "#wood"}
            }},
            # Central leg
            {"from": [6, 2, 6], "to": [10, 12, 10], "faces": {
                "north": {"texture": "#wood"}, "south": {"texture": "#wood"},
                "east": {"texture": "#wood"}, "west": {"texture": "#wood"}
            }},
            # Tabletop
            {"from": [0, 12, 0], "to": [16, 15, 16], "faces": {
                "north": {"texture": "#wood"}, "south": {"texture": "#wood"},
                "east": {"texture": "#wood"}, "west": {"texture": "#wood"},
                "up": {"texture": "#top", "uv": [0, 0, 16, 16]}, "down": {"texture": "#wood"}
            }}
        ]
    })

    # 3D Roulette Table Model
    save_json(os.path.join(ASSETS, "models", "block", "roulette_table.json"), {
        "textures": {
            "top": "skycasino:block/roulette_top",
            "wood": "skycasino:block/casino_counter",
            "gold": "skycasino:block/casino_gold_trim",
            "particle": "skycasino:block/roulette_top"
        },
        "elements": [
            {"from": [4, 0, 4], "to": [12, 2, 12], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                "up": {"texture": "#gold"}, "down": {"texture": "#wood"}
            }},
            {"from": [6, 2, 6], "to": [10, 12, 10], "faces": {
                "north": {"texture": "#wood"}, "south": {"texture": "#wood"},
                "east": {"texture": "#wood"}, "west": {"texture": "#wood"}
            }},
            {"from": [0, 12, 0], "to": [16, 15, 16], "faces": {
                "north": {"texture": "#wood"}, "south": {"texture": "#wood"},
                "east": {"texture": "#wood"}, "west": {"texture": "#wood"},
                "up": {"texture": "#top", "uv": [0, 0, 16, 16]}, "down": {"texture": "#wood"}
            }}
        ]
    })

    # 3D Fortune Wheel Model
    save_json(os.path.join(ASSETS, "models", "block", "fortune_wheel.json"), {
        "textures": {
            "wheel": "skycasino:block/fortune_wheel_face",
            "wood": "skycasino:block/casino_counter",
            "gold": "skycasino:block/casino_gold_trim",
            "particle": "skycasino:block/fortune_wheel_face"
        },
        "elements": [
            # Base
            {"from": [2, 0, 4], "to": [14, 2, 12], "faces": {
                "north": {"texture": "#wood"}, "south": {"texture": "#wood"},
                "east": {"texture": "#wood"}, "west": {"texture": "#wood"},
                "up": {"texture": "#gold"}, "down": {"texture": "#wood"}
            }},
            # Vertical post
            {"from": [7, 2, 9], "to": [9, 10, 11], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"}
            }},
            # Wheel disc
            {"from": [2, 3, 6], "to": [14, 15, 9], "faces": {
                "north": {"texture": "#wheel", "uv": [0, 0, 16, 16]},
                "south": {"texture": "#wheel", "uv": [0, 0, 16, 16]},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                "up": {"texture": "#gold"}, "down": {"texture": "#gold"}
            }},
            # Top pointer
            {"from": [7, 15, 6], "to": [9, 16, 8], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                "up": {"texture": "#gold"}
            }}
        ]
    })

    # 3D Casino Stool Model
    save_json(os.path.join(ASSETS, "models", "block", "casino_stool.json"), {
        "textures": {
            "velvet": "skycasino:block/casino_velvet",
            "gold": "skycasino:block/casino_gold_trim",
            "particle": "skycasino:block/casino_velvet"
        },
        "elements": [
            {"from": [4, 0, 4], "to": [12, 1, 12], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                "up": {"texture": "#gold"}, "down": {"texture": "#gold"}
            }},
            {"from": [7, 1, 7], "to": [9, 8, 9], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"}
            }},
            {"from": [3, 8, 3], "to": [13, 11, 13], "faces": {
                "north": {"texture": "#velvet"}, "south": {"texture": "#velvet"},
                "east": {"texture": "#velvet"}, "west": {"texture": "#velvet"},
                "up": {"texture": "#velvet"}, "down": {"texture": "#gold"}
            }}
        ]
    })

    # 3D Crystal Chandelier Model
    save_json(os.path.join(ASSETS, "models", "block", "casino_chandelier.json"), {
        "textures": {
            "gold": "skycasino:block/casino_gold_trim",
            "crystal": "skycasino:block/casino_crystal",
            "particle": "skycasino:block/casino_gold_trim"
        },
        "elements": [
            # Top chain & mount
            {"from": [6, 14, 6], "to": [10, 16, 10], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                "up": {"texture": "#gold"}, "down": {"texture": "#gold"}
            }},
            {"from": [7.5, 9, 7.5], "to": [8.5, 14, 8.5], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"}
            }},
            # Main gold ring
            {"from": [2, 8, 2], "to": [14, 9.5, 14], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                "up": {"texture": "#gold"}, "down": {"texture": "#gold"}
            }},
            # Crystal drops
            {"from": [3, 4, 3], "to": [5, 8, 5], "faces": {
                "north": {"texture": "#crystal"}, "south": {"texture": "#crystal"},
                "east": {"texture": "#crystal"}, "west": {"texture": "#crystal"}, "down": {"texture": "#crystal"}
            }},
            {"from": [11, 4, 3], "to": [13, 8, 5], "faces": {
                "north": {"texture": "#crystal"}, "south": {"texture": "#crystal"},
                "east": {"texture": "#crystal"}, "west": {"texture": "#crystal"}, "down": {"texture": "#crystal"}
            }},
            {"from": [3, 4, 11], "to": [5, 8, 13], "faces": {
                "north": {"texture": "#crystal"}, "south": {"texture": "#crystal"},
                "east": {"texture": "#crystal"}, "west": {"texture": "#crystal"}, "down": {"texture": "#crystal"}
            }},
            {"from": [11, 4, 11], "to": [13, 8, 13], "faces": {
                "north": {"texture": "#crystal"}, "south": {"texture": "#crystal"},
                "east": {"texture": "#crystal"}, "west": {"texture": "#crystal"}, "down": {"texture": "#crystal"}
            }},
            # Center glowing crystal core
            {"from": [6.5, 2.5, 6.5], "to": [9.5, 8, 9.5], "faces": {
                "north": {"texture": "#crystal"}, "south": {"texture": "#crystal"},
                "east": {"texture": "#crystal"}, "west": {"texture": "#crystal"}, "down": {"texture": "#crystal"}
            }}
        ]
    })

    # Directional blockstates + item models for machines
    for name in ["cashier_terminal", "slot_machine", "blackjack_table", "roulette_table", "fortune_wheel"]:
        save_json(os.path.join(ASSETS, "blockstates", f"{name}.json"), {
            "variants": {
                "facing=north": {"model": f"skycasino:block/{name}"},
                "facing=east":  {"model": f"skycasino:block/{name}", "y": 90},
                "facing=south": {"model": f"skycasino:block/{name}", "y": 180},
                "facing=west":  {"model": f"skycasino:block/{name}", "y": 270}
            }
        })
        save_json(os.path.join(ASSETS, "models", "item", f"{name}.json"), {
            "parent": f"skycasino:block/{name}"
        })

    # Non-directional 3D decor blockstates + item models
    for name in ["casino_stool", "casino_chandelier"]:
        save_json(os.path.join(ASSETS, "blockstates", f"{name}.json"), {
            "variants": {"": {"model": f"skycasino:block/{name}"}}
        })
        save_json(os.path.join(ASSETS, "models", "item", f"{name}.json"), {
            "parent": f"skycasino:block/{name}"
        })

    # Chip item models
    for chip in ["white_chip", "red_chip", "blue_chip", "purple_chip", "gold_chip"]:
        save_json(os.path.join(ASSETS, "models", "item", f"{chip}.json"), {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"skycasino:item/{chip}"}
        })

# ==========================================
# 3. LOOT TABLES & CRAFTING RECIPES
# ==========================================
def generate_data():
    all_blocks = [
        "cashier_terminal", "slot_machine", "blackjack_table", "roulette_table", "fortune_wheel",
        "casino_carpet_red", "casino_carpet_green", "gold_marble", "black_gold_marble",
        "neon_gold", "neon_pink", "neon_cyan", "neon_emerald",
        "casino_counter", "casino_sign_block", "casino_chandelier", "casino_stool"
    ]
    for b in all_blocks:
        save_json(os.path.join(DATA, "loot_tables", "blocks", f"{b}.json"), {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "entries": [{"type": "minecraft:item", "name": f"skycasino:{b}"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}]
            }]
        })

    # Recipes
    recipes = {
        "cashier_terminal": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GGG", "GEG", "IRI"],
            "key": {
                "G": {"item": "minecraft:gold_ingot"},
                "E": {"item": "minecraft:emerald"},
                "I": {"item": "minecraft:iron_ingot"},
                "R": {"item": "minecraft:redstone"}
            },
            "result": {"item": "skycasino:cashier_terminal"}
        },
        "slot_machine": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GGG", "RDR", "GBG"],
            "key": {
                "G": {"item": "minecraft:gold_ingot"},
                "R": {"item": "minecraft:redstone_lamp"},
                "D": {"item": "minecraft:diamond"},
                "B": {"item": "minecraft:iron_block"}
            },
            "result": {"item": "skycasino:slot_machine"}
        },
        "blackjack_table": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["CCC", "GPG", " P "],
            "key": {
                "C": {"item": "minecraft:green_carpet"},
                "G": {"item": "minecraft:gold_ingot"},
                "P": {"item": "minecraft:dark_oak_planks"}
            },
            "result": {"item": "skycasino:blackjack_table"}
        },
        "roulette_table": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GCG", "RPR", " P "],
            "key": {
                "C": {"item": "minecraft:green_carpet"},
                "G": {"item": "minecraft:gold_ingot"},
                "R": {"item": "minecraft:red_wool"},
                "P": {"item": "minecraft:dark_oak_planks"}
            },
            "result": {"item": "skycasino:roulette_table"}
        },
        "fortune_wheel": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GYG", "RDB", " P "],
            "key": {
                "G": {"item": "minecraft:gold_ingot"},
                "Y": {"item": "minecraft:yellow_wool"},
                "R": {"item": "minecraft:red_wool"},
                "B": {"item": "minecraft:blue_wool"},
                "D": {"item": "minecraft:diamond"},
                "P": {"item": "minecraft:dark_oak_planks"}
            },
            "result": {"item": "skycasino:fortune_wheel"}
        },
        "casino_stool": {
            "type": "minecraft:crafting_shaped",
            "pattern": [" R ", " G ", "GGG"],
            "key": {
                "R": {"item": "minecraft:red_wool"},
                "G": {"item": "minecraft:gold_nugget"}
            },
            "result": {"item": "skycasino:casino_stool", "count": 2}
        },
        "casino_chandelier": {
            "type": "minecraft:crafting_shaped",
            "pattern": [" G ", "GAG", " A "],
            "key": {
                "G": {"item": "minecraft:gold_ingot"},
                "A": {"item": "minecraft:glowstone_dust"}
            },
            "result": {"item": "skycasino:casino_chandelier"}
        },
        "casino_counter": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GGG", "PPP", "PPP"],
            "key": {
                "G": {"item": "minecraft:gold_nugget"},
                "P": {"item": "minecraft:dark_oak_planks"}
            },
            "result": {"item": "skycasino:casino_counter", "count": 4}
        },
        "casino_carpet_red": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["RGR"],
            "key": {
                "R": {"item": "minecraft:red_carpet"},
                "G": {"item": "minecraft:gold_nugget"}
            },
            "result": {"item": "skycasino:casino_carpet_red", "count": 4}
        },
        "casino_carpet_green": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["EGE"],
            "key": {
                "E": {"item": "minecraft:green_carpet"},
                "G": {"item": "minecraft:gold_nugget"}
            },
            "result": {"item": "skycasino:casino_carpet_green", "count": 4}
        },
        "gold_marble": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["QQ", "QG"],
            "key": {
                "Q": {"item": "minecraft:quartz_block"},
                "G": {"item": "minecraft:gold_nugget"}
            },
            "result": {"item": "skycasino:gold_marble", "count": 4}
        },
        "black_gold_marble": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["BB", "BG"],
            "key": {
                "B": {"item": "minecraft:polished_blackstone"},
                "G": {"item": "minecraft:gold_nugget"}
            },
            "result": {"item": "skycasino:black_gold_marble", "count": 4}
        },
        "neon_gold": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GGG", "GDG", "GGG"],
            "key": {
                "G": {"item": "minecraft:glass"},
                "D": {"item": "minecraft:glowstone"}
            },
            "result": {"item": "skycasino:neon_gold", "count": 4}
        },
        "neon_pink": {
            "type": "minecraft:crafting_shapeless",
            "ingredients": [{"item": "skycasino:neon_gold"}, {"item": "minecraft:magenta_dye"}],
            "result": {"item": "skycasino:neon_pink"}
        },
        "neon_cyan": {
            "type": "minecraft:crafting_shapeless",
            "ingredients": [{"item": "skycasino:neon_gold"}, {"item": "minecraft:cyan_dye"}],
            "result": {"item": "skycasino:neon_cyan"}
        },
        "neon_emerald": {
            "type": "minecraft:crafting_shapeless",
            "ingredients": [{"item": "skycasino:neon_gold"}, {"item": "minecraft:lime_dye"}],
            "result": {"item": "skycasino:neon_emerald"}
        },
        "casino_sign_block": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GNG", "NEN", "GNG"],
            "key": {
                "G": {"item": "minecraft:gold_ingot"},
                "N": {"item": "skycasino:neon_gold"},
                "E": {"item": "minecraft:glowstone"}
            },
            "result": {"item": "skycasino:casino_sign_block", "count": 2}
        }
    }
    for rname, rdata in recipes.items():
        save_json(os.path.join(DATA, "recipes", f"{rname}.json"), rdata)

# ==========================================
# 4. RUSSIAN & ENGLISH LOCALIZATION
# ==========================================
def generate_lang():
    en = {
        "creativetab.skycasino": "Sky Casino",

        "item.skycasino.white_chip": "White Casino Chip (1)",
        "item.skycasino.red_chip": "Red Casino Chip (5)",
        "item.skycasino.blue_chip": "Blue Casino Chip (25)",
        "item.skycasino.purple_chip": "Purple Casino Chip (100)",
        "item.skycasino.gold_chip": "Gold VIP Chip (1,000)",

        "tooltip.skycasino.chip_value": "Value: %s chips",
        "tooltip.skycasino.chip_stack_total": "Stack total: %s chips",

        "block.skycasino.cashier_terminal": "Cashier Terminal",
        "block.skycasino.slot_machine": "Slot Machine",
        "block.skycasino.blackjack_table": "Blackjack Table",
        "block.skycasino.roulette_table": "Roulette Table",
        "block.skycasino.fortune_wheel": "Wheel of Fortune",
        "block.skycasino.casino_carpet_red": "Ruby-Gold Casino Carpet",
        "block.skycasino.casino_carpet_green": "Emerald-Gold Casino Carpet",
        "block.skycasino.gold_marble": "Gold-Veined Marble",
        "block.skycasino.black_gold_marble": "Obsidian-Gold Marble",
        "block.skycasino.neon_gold": "Golden Neon Block",
        "block.skycasino.neon_pink": "Magenta Neon Block",
        "block.skycasino.neon_cyan": "Cyan Neon Block",
        "block.skycasino.neon_emerald": "Emerald Neon Block",
        "block.skycasino.casino_counter": "Mahogany Casino Counter",
        "block.skycasino.casino_sign_block": "Glowing Casino Sign",
        "block.skycasino.casino_chandelier": "Crystal Casino Chandelier",
        "block.skycasino.casino_stool": "Velvet Casino Stool",

        "screen.skycasino.cashier": "Casino Cashier Terminal",
        "screen.skycasino.slot_machine": "Lucky 777 Slot Machine",
        "screen.skycasino.blackjack": "VIP Blackjack (21)",
        "screen.skycasino.roulette": "European Roulette",
        "screen.skycasino.fortune_wheel": "Wheel of Fortune",

        "gui.skycasino.all": "All",
        "gui.skycasino.close": "Close",
        "gui.skycasino.spin": "SPIN!",
        "gui.skycasino.consolidate": "Consolidate Chips",
        "gui.skycasino.resource": "Resource",
        "gui.skycasino.buy_chips": "Buy Chips",
        "gui.skycasino.cash_out": "Cash Out",
        "gui.skycasino.payouts": "Payouts",
        "gui.skycasino.jackpot": "JACKPOT! +%s",
        "gui.skycasino.won_chips": "WIN! +%s",
        "gui.skycasino.try_again": "No win",
        "gui.skycasino.not_enough_chips": "Need chips!",

        "gui.skycasino.bj.deal": "Deal",
        "gui.skycasino.bj.hit": "Hit",
        "gui.skycasino.bj.stand": "Stand",
        "gui.skycasino.bj.double": "x2",
        "gui.skycasino.bj.dealer": "Dealer",
        "gui.skycasino.bj.player": "You",
        "gui.skycasino.bj.status.blackjack": "BLACKJACK! +%s",
        "gui.skycasino.bj.status.win": "YOU WIN! +%s",
        "gui.skycasino.bj.status.push": "PUSH (Refund)",
        "gui.skycasino.bj.status.bust": "BUST!",
        "gui.skycasino.bj.status.lose": "Dealer wins",
        "gui.skycasino.bj.status.turn": "Your turn",
        "gui.skycasino.bj.status.ready": "Place bet",

        "gui.skycasino.rl.red": "Red",
        "gui.skycasino.rl.black": "Black",
        "gui.skycasino.rl.even": "Even",
        "gui.skycasino.rl.odd": "Odd",
        "gui.skycasino.rl.current_bet": "Selected bet: %s",
        "gui.skycasino.rl.history": "History:",
        "gui.skycasino.rl.landed": "Landed: %s",

        "gui.skycasino.fw.prizes": "Multipliers"
    }

    ru = {
        "creativetab.skycasino": "Sky Casino",

        "item.skycasino.white_chip": "Белая фишка казино (1)",
        "item.skycasino.red_chip": "Красная фишка казино (5)",
        "item.skycasino.blue_chip": "Синяя фишка казино (25)",
        "item.skycasino.purple_chip": "Фиолетовая фишка казино (100)",
        "item.skycasino.gold_chip": "Золотая VIP-фишка (1 000)",

        "tooltip.skycasino.chip_value": "Номинал: %s фишек",
        "tooltip.skycasino.chip_stack_total": "Всего в стаке: %s фишек",

        "block.skycasino.cashier_terminal": "Кассирский терминал",
        "block.skycasino.slot_machine": "Игровой автомат",
        "block.skycasino.blackjack_table": "Стол для Блэкджека",
        "block.skycasino.roulette_table": "Стол Рулетки",
        "block.skycasino.fortune_wheel": "Колесо Фортуны",
        "block.skycasino.casino_carpet_red": "Рубиново-золотой ковёр казино",
        "block.skycasino.casino_carpet_green": "Изумрудно-золотой ковёр казино",
        "block.skycasino.gold_marble": "Белый мрамор с золотом",
        "block.skycasino.black_gold_marble": "Чёрный мрамор с золотом",
        "block.skycasino.neon_gold": "Золотой неоновый блок",
        "block.skycasino.neon_pink": "Розовый неоновый блок",
        "block.skycasino.neon_cyan": "Голубой неоновый блок",
        "block.skycasino.neon_emerald": "Изумрудный неоновый блок",
        "block.skycasino.casino_counter": "Стойка казино из красного дерева",
        "block.skycasino.casino_sign_block": "Светящаяся вывеска казино",
        "block.skycasino.casino_chandelier": "Хрустальная люстра казино",
        "block.skycasino.casino_stool": "Бархатный табурет казино",

        "screen.skycasino.cashier": "Кассирский терминал",
        "screen.skycasino.slot_machine": "Игровой автомат 777",
        "screen.skycasino.blackjack": "VIP Блэкджек (21)",
        "screen.skycasino.roulette": "Европейская Рулетка",
        "screen.skycasino.fortune_wheel": "Колесо Фортуны",

        "gui.skycasino.all": "Всё",
        "gui.skycasino.close": "Выход",
        "gui.skycasino.spin": "КРУТИТЬ!",
        "gui.skycasino.consolidate": "Укрупнить фишки",
        "gui.skycasino.resource": "Ресурс",
        "gui.skycasino.buy_chips": "Купить фишки",
        "gui.skycasino.cash_out": "Вывести",
        "gui.skycasino.payouts": "Выплаты",
        "gui.skycasino.jackpot": "ДЖЕКПОТ! +%s",
        "gui.skycasino.won_chips": "ВЫИГРЫШ! +%s",
        "gui.skycasino.try_again": "Мимо...",
        "gui.skycasino.not_enough_chips": "Мало фишек!",

        "gui.skycasino.bj.deal": "Раздать",
        "gui.skycasino.bj.hit": "Ещё",
        "gui.skycasino.bj.stand": "Хватит",
        "gui.skycasino.bj.double": "×2",
        "gui.skycasino.bj.dealer": "Дилер",
        "gui.skycasino.bj.player": "Вы",
        "gui.skycasino.bj.status.blackjack": "БЛЭКДЖЕК! +%s",
        "gui.skycasino.bj.status.win": "ПОБЕДА! +%s",
        "gui.skycasino.bj.status.push": "НИЧЬЯ (Возврат)",
        "gui.skycasino.bj.status.bust": "ПЕРЕБОР!",
        "gui.skycasino.bj.status.lose": "Дилер выиграл",
        "gui.skycasino.bj.status.turn": "Ваш ход",
        "gui.skycasino.bj.status.ready": "Сделайте ставку",

        "gui.skycasino.rl.red": "Красное",
        "gui.skycasino.rl.black": "Чёрное",
        "gui.skycasino.rl.even": "Чёт",
        "gui.skycasino.rl.odd": "Нечет",
        "gui.skycasino.rl.current_bet": "Выбрана ставка: %s",
        "gui.skycasino.rl.history": "История:",
        "gui.skycasino.rl.landed": "Выпало: %s",

        "gui.skycasino.fw.prizes": "Множители"
    }

    save_json(os.path.join(ASSETS, "lang", "en_us.json"), en)
    save_json(os.path.join(ASSETS, "lang", "ru_ru.json"), ru)

if __name__ == "__main__":
    generate_textures()
    generate_models_and_blockstates()
    generate_data()
    generate_lang()
    print("All Sky Casino assets, models, recipes, loot tables, and localizations generated!")
