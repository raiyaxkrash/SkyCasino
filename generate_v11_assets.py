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

def draw_chip_texture(main_rgb, edge_rgb, center_rgb, accent_rgb):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cx, cy = 7.5, 7.5
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            if d <= 6.8:
                if d >= 5.2:
                    angle = math.degrees(math.atan2(y - cy, x - cx)) % 60
                    px[x, y] = (*edge_rgb, 255) if angle < 22 else (*main_rgb, 255)
                elif d >= 4.2:
                    px[x, y] = (*accent_rgb, 255)
                else:
                    shade = 1.08 if (x + y < 14) else 0.92
                    px[x, y] = (min(255, int(center_rgb[0] * shade)),
                                min(255, int(center_rgb[1] * shade)),
                                min(255, int(center_rgb[2] * shade)), 255)
                if 6.1 <= d <= 6.8:
                    r, g, b, a = px[x, y]
                    px[x, y] = (int(r * 0.6), int(g * 0.6), int(b * 0.6), 255)
    for ox, oy in [(7, 7), (8, 8), (7, 8), (8, 7)]:
        px[ox, oy] = (*accent_rgb, 255)
    return img

def generate_v11():
    # 1. Mega Chips
    save_png("item/emerald_chip.png", draw_chip_texture((22, 175, 88), (255, 225, 95), (52, 225, 128), (220, 255, 235)))
    save_png("item/netherite_chip.png", draw_chip_texture((48, 32, 62), (255, 195, 55), (110, 45, 155), (255, 235, 130)))

    # 2. Pocket Casino Tablet (16x16 sleek dark-gold handheld with glowing purple/gold screen)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 1, 13, 14], fill=(28, 22, 36, 255), outline=(235, 185, 48, 255))
    d.rectangle([4, 3, 11, 10], fill=(18, 65, 54, 255), outline=(180, 95, 255, 255))
    # Tiny golden "7" & sparkle on screen
    d.line([6, 5, 9, 5], fill=(255, 225, 70, 255))
    d.line([9, 5, 7, 9], fill=(255, 225, 70, 255))
    # Buttons at bottom of tablet
    d.point((5, 12), fill=(255, 70, 90, 255))
    d.point((7, 12), fill=(60, 235, 130, 255))
    d.point((8, 12), fill=(60, 235, 130, 255))
    d.point((10, 12), fill=(70, 200, 255, 255))
    save_png("item/pocket_casino.png", img)

    # 3. VIP Card (16x16 black-gold luxury credit card)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([1, 3, 14, 12], fill=(22, 20, 28, 255), outline=(242, 192, 52, 255))
    d.rectangle([2, 5, 13, 6], fill=(215, 162, 35, 255))
    # Golden EMV chip
    d.rectangle([3, 8, 5, 10], fill=(255, 228, 105, 255))
    # VIP sparkle
    d.rectangle([10, 8, 12, 10], fill=(185, 85, 255, 255))
    d.point((11, 9), fill=(255, 245, 190, 255))
    save_png("item/vip_card.png", img)

    # 4. Crash Machine Front (32x32 dark space screen + cyan rising curve + rocket)
    img = Image.new("RGBA", (32, 32), (24, 20, 42, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 31, 31], outline=(235, 185, 48, 255), width=2)
    d.rectangle([3, 3, 28, 22], fill=(10, 14, 30, 255), outline=(45, 225, 245, 255))
    # Rising exponential curve
    pts = [(5, 20), (9, 19), (13, 17), (17, 14), (21, 10), (25, 6)]
    for i in range(len(pts) - 1):
        d.line([pts[i], pts[i+1]], fill=(55, 235, 255, 255), width=2)
    # Rocket tip
    d.rectangle([23, 5, 26, 8], fill=(255, 75, 85, 255))
    d.point((25, 5), fill=(255, 235, 110, 255))
    # Controls
    d.rectangle([5, 25, 14, 28], fill=(40, 215, 115, 255))
    d.rectangle([17, 25, 26, 28], fill=(245, 195, 45, 255))
    save_png("block/crash_machine_front.png", img)

    # 5. Mines Machine Front (32x32 dark cabinet + 5x5 diamond/TNT grid)
    img = Image.new("RGBA", (32, 32), (20, 28, 46, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 31, 31], outline=(235, 185, 48, 255), width=2)
    d.rectangle([3, 3, 28, 23], fill=(12, 16, 26, 255), outline=(85, 195, 255, 255))
    for r in range(4):
        for c in range(5):
            x0 = 5 + c * 4 + c // 2
            y0 = 5 + r * 4 + r // 2
            if (r, c) in [(0, 1), (1, 3), (2, 2), (3, 0)]:
                col = (55, 235, 255, 255)  # Diamond
            elif (r, c) == (1, 1):
                col = (225, 45, 55, 255)   # TNT
            else:
                col = (42, 52, 74, 255)
            d.rectangle([x0, y0, x0 + 2, y0 + 2], fill=col)
    d.rectangle([6, 25, 25, 28], fill=(245, 195, 45, 255))
    save_png("block/mines_machine_front.png", img)

    # Item models
    for item in ["emerald_chip", "netherite_chip", "pocket_casino", "vip_card"]:
        save_json(os.path.join(ASSETS, "models", "item", f"{item}.json"), {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"skycasino:item/{item}"}
        })

    # 3D Block models + blockstates + loot tables for Crash & Mines
    for name, front_tex in [("crash_machine", "crash_machine_front"), ("mines_machine", "mines_machine_front")]:
        save_json(os.path.join(ASSETS, "models", "block", f"{name}.json"), {
            "textures": {
                "front": f"skycasino:block/{front_tex}",
                "side": "skycasino:block/black_gold_marble",
                "gold": "skycasino:block/casino_gold_trim",
                "particle": f"skycasino:block/{front_tex}"
            },
            "elements": [
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
                {
                    "from": [3, 14, 4], "to": [13, 16, 12],
                    "faces": {
                        "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                        "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                        "up": {"texture": "#gold"}
                    }
                }
            ]
        })
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
        save_json(os.path.join(DATA, "loot_tables", "blocks", f"{name}.json"), {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "entries": [{"type": "minecraft:item", "name": f"skycasino:{name}"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}]
            }]
        })

    # New Recipes
    new_recipes = {
        "pocket_casino": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GDG", "GCG", "RER"],
            "key": {
                "G": {"item": "minecraft:gold_ingot"},
                "D": {"item": "minecraft:diamond"},
                "C": {"item": "skycasino:cashier_terminal"},
                "R": {"item": "minecraft:redstone"},
                "E": {"item": "minecraft:ender_pearl"}
            },
            "result": {"item": "skycasino:pocket_casino"}
        },
        "vip_card": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GGG", "ODO", "GGG"],
            "key": {
                "G": {"item": "minecraft:gold_ingot"},
                "O": {"item": "minecraft:obsidian"},
                "D": {"item": "minecraft:diamond"}
            },
            "result": {"item": "skycasino:vip_card"}
        },
        "crash_machine": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GGG", "RFR", "GBG"],
            "key": {
                "G": {"item": "minecraft:gold_ingot"},
                "R": {"item": "minecraft:redstone_lamp"},
                "F": {"item": "minecraft:firework_rocket"},
                "B": {"item": "minecraft:iron_block"}
            },
            "result": {"item": "skycasino:crash_machine"}
        },
        "mines_machine": {
            "type": "minecraft:crafting_shaped",
            "pattern": ["GGG", "DTD", "GBG"],
            "key": {
                "G": {"item": "minecraft:gold_ingot"},
                "D": {"item": "minecraft:diamond"},
                "T": {"item": "minecraft:tnt"},
                "B": {"item": "minecraft:iron_block"}
            },
            "result": {"item": "skycasino:mines_machine"}
        }
    }
    for rname, rdata in new_recipes.items():
        save_json(os.path.join(DATA, "recipes", f"{rname}.json"), rdata)

    # Merge new translations into en_us.json and ru_ru.json
    en_add = {
        "item.skycasino.pocket_casino": "Pocket Casino Tablet",
        "item.skycasino.vip_card": "VIP Casino Card",
        "item.skycasino.emerald_chip": "Emerald Mega Chip (10,000)",
        "item.skycasino.netherite_chip": "Star Mega Chip (100,000)",
        "tooltip.skycasino.pocket_hint": "Right-click to open all casino games anywhere!",
        "tooltip.skycasino.vip_balance": "Card Balance: %s chips",
        "tooltip.skycasino.vip_hint": "Auto-stores winnings & bets. Right-click to deposit loose chips.",
        "msg.skycasino.vip_deposited": "Deposited %s chips to VIP Card! Balance: %s",
        "msg.skycasino.vip_balance": "VIP Card Balance: %s chips",
        "block.skycasino.crash_machine": "Crash Rocket Arcade",
        "block.skycasino.mines_machine": "Mines 5x5 Arcade",
        "screen.skycasino.pocket_hub": "Pocket Casino — VIP Hub",
        "screen.skycasino.crash": "Crash Rocket",
        "screen.skycasino.mines": "Mines 5x5",
        "gui.skycasino.vip_active": "VIP Card active (auto-sync)",
        "gui.skycasino.vip_missing": "Tip: Craft a VIP Card for infinite storage",
        "gui.skycasino.crash.launch": "LAUNCH!",
        "gui.skycasino.crash.cashout": "CASH OUT!",
        "gui.skycasino.crash.potential": "Win: %s chips",
        "gui.skycasino.crash.exploded": "CRASHED!",
        "gui.skycasino.mines.bet": "Bet:",
        "gui.skycasino.mines.count": "Mines:",
        "gui.skycasino.mines.start": "Start Game",
        "gui.skycasino.mines.boom": "BOOM! Mine hit"
    }

    ru_add = {
        "item.skycasino.pocket_casino": "Портативный планшет казино",
        "item.skycasino.vip_card": "VIP-карта казино",
        "item.skycasino.emerald_chip": "Изумрудная мега-фишка (10 000)",
        "item.skycasino.netherite_chip": "Звёздная мега-фишка (100 000)",
        "tooltip.skycasino.pocket_hint": "ПКМ — открыть все игры казино и кассира в любом месте!",
        "tooltip.skycasino.vip_balance": "Баланс карты: %s фишек",
        "tooltip.skycasino.vip_hint": "Автоматически хранит все выигрыши. ПКМ — зачислить фишки из инвентаря.",
        "msg.skycasino.vip_deposited": "Зачислено %s фишек на VIP-карту! Баланс: %s",
        "msg.skycasino.vip_balance": "Баланс VIP-карты: %s фишек",
        "block.skycasino.crash_machine": "Автомат «Краш (Ракета)»",
        "block.skycasino.mines_machine": "Автомат «Мины 5×5»",
        "screen.skycasino.pocket_hub": "Портативное казино — VIP Хаб",
        "screen.skycasino.crash": "Краш — Ракета",
        "screen.skycasino.mines": "Мины 5×5",
        "gui.skycasino.vip_active": "VIP-карта активна (авто-баланс)",
        "gui.skycasino.vip_missing": "Совет: скрафти VIP-карту для хранения фишек",
        "gui.skycasino.crash.launch": "ЗАПУСК!",
        "gui.skycasino.crash.cashout": "ЗАБРАТЬ!",
        "gui.skycasino.crash.potential": "Забрать сейчас: %s",
        "gui.skycasino.crash.exploded": "РАКЕТА ВЗОРВАЛАСЬ!",
        "gui.skycasino.mines.bet": "Ставка:",
        "gui.skycasino.mines.count": "Кол-во мин:",
        "gui.skycasino.mines.start": "Начать игру",
        "gui.skycasino.mines.boom": "БУМ! Вы попали на мину"
    }

    for fname, adds in [("en_us.json", en_add), ("ru_ru.json", ru_add)]:
        p = os.path.join(ASSETS, "lang", fname)
        with open(p, "r", encoding="utf-8") as f:
            data = json.load(f)
        data.update(adds)
        save_json(p, data)

    print("Sky Casino v1.1.0 assets generated successfully!")

if __name__ == "__main__":
    generate_v11()
