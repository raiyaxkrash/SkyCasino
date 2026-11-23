import json
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

def generate_v12():
    # 1. Classic Loot Case (16x16 sleek cyan/steel treasure case with golden clasp)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 4, 13, 13], fill=(32, 72, 118, 255), outline=(56, 189, 248, 255))
    d.rectangle([3, 6, 12, 8], fill=(22, 48, 86, 255))
    d.rectangle([6, 2, 9, 4], outline=(235, 185, 48, 255)) # handle
    d.rectangle([7, 7, 8, 10], fill=(255, 220, 75, 255))  # gold lock
    d.point((4, 5), fill=(180, 245, 255, 255))
    d.point((11, 5), fill=(180, 245, 255, 255))
    save_png("item/loot_case_classic.png", img)

    # 2. VIP Loot Case (16x16 luxury obsidian-gold-amethyst chest)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 4, 13, 13], fill=(48, 18, 62, 255), outline=(255, 210, 60, 255))
    d.rectangle([3, 6, 12, 8], fill=(140, 35, 165, 255))
    d.rectangle([6, 2, 9, 4], outline=(255, 225, 95, 255))
    d.rectangle([6, 7, 9, 10], fill=(255, 235, 110, 255))
    d.point((7, 8), fill=(255, 80, 180, 255))
    d.point((8, 8), fill=(255, 80, 180, 255))
    save_png("item/loot_case_vip.png", img)

    # 3. Prize Shop Front (32x32 glowing golden showcase with trophy/diamond inside)
    img = Image.new("RGBA", (32, 32), (38, 18, 32, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 31, 31], outline=(242, 192, 52, 255), width=2)
    # Glass display window
    d.rectangle([3, 4, 28, 22], fill=(20, 38, 58, 255), outline=(125, 225, 255, 255))
    # Glowing diamond trophy inside
    d.polygon([(15, 7), (21, 13), (15, 19), (9, 13)], fill=(65, 235, 255, 255), outline=(225, 255, 255, 255))
    d.rectangle([12, 19, 18, 21], fill=(245, 195, 45, 255))
    # Pedestal drawers
    d.rectangle([4, 24, 27, 29], fill=(92, 24, 34, 255), outline=(235, 185, 48, 255))
    d.rectangle([13, 26, 18, 27], fill=(255, 225, 85, 255))
    save_png("block/prize_shop_front.png", img)

    # Item models
    for item in ["loot_case_classic", "loot_case_vip"]:
        save_json(os.path.join(ASSETS, "models", "item", f"{item}.json"), {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"skycasino:item/{item}"}
        })

    # 3D Prize Shop Block Model
    save_json(os.path.join(ASSETS, "models", "block", "prize_shop.json"), {
        "textures": {
            "front": "skycasino:block/prize_shop_front",
            "side": "skycasino:block/gold_marble",
            "gold": "skycasino:block/casino_gold_trim",
            "particle": "skycasino:block/prize_shop_front"
        },
        "elements": [
            {"from": [1, 0, 1], "to": [15, 5, 15], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"},
                "up": {"texture": "#gold"}, "down": {"texture": "#side"}
            }},
            {"from": [2, 5, 2], "to": [14, 15, 14], "faces": {
                "north": {"texture": "#front", "uv": [0, 0, 16, 16]},
                "south": {"texture": "#side"}, "east": {"texture": "#side"}, "west": {"texture": "#side"},
                "up": {"texture": "#gold"}
            }},
            {"from": [3, 15, 3], "to": [13, 16, 13], "faces": {
                "north": {"texture": "#gold"}, "south": {"texture": "#gold"},
                "east": {"texture": "#gold"}, "west": {"texture": "#gold"}, "up": {"texture": "#gold"}
            }}
        ]
    })

    save_json(os.path.join(ASSETS, "blockstates", "prize_shop.json"), {
        "variants": {
            "facing=north": {"model": "skycasino:block/prize_shop"},
            "facing=east":  {"model": "skycasino:block/prize_shop", "y": 90},
            "facing=south": {"model": "skycasino:block/prize_shop", "y": 180},
            "facing=west":  {"model": "skycasino:block/prize_shop", "y": 270}
        }
    })
    save_json(os.path.join(ASSETS, "models", "item", "prize_shop.json"), {
        "parent": "skycasino:block/prize_shop"
    })
    save_json(os.path.join(DATA, "loot_tables", "blocks", "prize_shop.json"), {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": "skycasino:prize_shop"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]
        }]
    })
    save_json(os.path.join(DATA, "recipes", "prize_shop.json"), {
        "type": "minecraft:crafting_shaped",
        "pattern": ["GGG", "QDQ", "GCG"],
        "key": {
            "G": {"item": "minecraft:gold_ingot"},
            "Q": {"item": "minecraft:glass"},
            "D": {"item": "minecraft:diamond"},
            "C": {"item": "minecraft:chest"}
        },
        "result": {"item": "skycasino:prize_shop"}
    })

    # Merge Translations
    en_add = {
        "item.skycasino.loot_case_classic": "Classic Casino Loot Case",
        "item.skycasino.loot_case_vip": "VIP Casino Loot Case",
        "tooltip.skycasino.case_classic": "Right-click to spin the Loot Case Roulette!",
        "tooltip.skycasino.case_vip": "Right-click to spin for Mythic & ATM9 rewards!",
        "block.skycasino.prize_shop": "Casino Prize Shop",
        "screen.skycasino.prize_shop": "Casino Prize Shop",
        "screen.skycasino.case_classic": "Classic Loot Case Roulette",
        "screen.skycasino.case_vip": "VIP Loot Case Roulette",
        "gui.skycasino.buy": "Buy",
        "gui.skycasino.open_classic": "Open Classic Case",
        "gui.skycasino.open_vip": "Open VIP Case",
        "gui.skycasino.case.open": "OPEN CASE!",
        "gui.skycasino.case.owned": "Cases in inventory: %s",
        "gui.skycasino.case.hint": "Buy more cases in the Prize Shop!",
        "rarity.skycasino.common": "COMMON",
        "rarity.skycasino.rare": "RARE",
        "rarity.skycasino.epic": "EPIC!",
        "rarity.skycasino.legendary": "LEGENDARY!!",
        "rarity.skycasino.mythic": "★ MYTHIC JACKPOT! ★"
    }

    ru_add = {
        "item.skycasino.loot_case_classic": "Обычный Луд-кейс казино",
        "item.skycasino.loot_case_vip": "VIP Луд-кейс казино",
        "tooltip.skycasino.case_classic": "ПКМ — запустить рулетку открытия кейса!",
        "tooltip.skycasino.case_vip": "ПКМ — шанс выбить Мифический дроп и слитки ATM9!",
        "block.skycasino.prize_shop": "Магазин призов казино",
        "screen.skycasino.prize_shop": "Магазин призов казино",
        "screen.skycasino.case_classic": "Рулетка — Обычный кейс",
        "screen.skycasino.case_vip": "Рулетка — VIP кейс",
        "gui.skycasino.buy": "Купить",
        "gui.skycasino.open_classic": "Открыть Обычный",
        "gui.skycasino.open_vip": "Открыть VIP-кейс",
        "gui.skycasino.case.open": "ОТКРЫТЬ КЕЙС!",
        "gui.skycasino.case.owned": "Кейсов в инвентаре: %s",
        "gui.skycasino.case.hint": "Кейсы можно купить в Магазине призов!",
        "rarity.skycasino.common": "ОБЫЧНЫЙ",
        "rarity.skycasino.rare": "РЕДКИЙ",
        "rarity.skycasino.epic": "ЭПИЧЕСКИЙ!",
        "rarity.skycasino.legendary": "ЛЕГЕНДАРНЫЙ!!",
        "rarity.skycasino.mythic": "★ МИФИЧЕСКИЙ ДРОП! ★"
    }

    for fname, adds in [("en_us.json", en_add), ("ru_ru.json", ru_add)]:
        p = os.path.join(ASSETS, "lang", fname)
        with open(p, "r", encoding="utf-8") as f:
            data = json.load(f)
        data.update(adds)
        save_json(p, data)

    print("Sky Casino v1.2.0 assets generated successfully!")

if __name__ == "__main__":
    generate_v12()
