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

def generate_v15():
    # Anti-Tilt Coffee mug pixel art (16x16 steaming luxury espresso mug with gold rim)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Steam wisps
    d.point((6, 2), fill=(210, 245, 255, 210))
    d.point((7, 3), fill=(210, 245, 255, 230))
    d.point((9, 1), fill=(210, 245, 255, 200))
    d.point((10, 3), fill=(210, 245, 255, 220))
    # Cup body
    d.rectangle([4, 5, 11, 13], fill=(45, 38, 52, 255), outline=(235, 185, 48, 255))
    d.rectangle([5, 6, 10, 7], fill=(110, 62, 32, 255))  # rich coffee top
    d.rectangle([6, 9, 9, 11], fill=(56, 215, 245, 255)) # glowing anti-tilt cyan gem
    # Mug handle
    d.rectangle([12, 7, 13, 11], outline=(235, 185, 48, 255))
    save_png("item/anti_tilt_coffee.png", img)

    save_json(os.path.join(ASSETS, "models", "item", "anti_tilt_coffee.json"), {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": "skycasino:item/anti_tilt_coffee"}
    })

    save_json(os.path.join(DATA, "recipes", "anti_tilt_coffee.json"), {
        "type": "minecraft:crafting_shapeless",
        "ingredients": [
            {"item": "minecraft:glass_bottle"},
            {"item": "minecraft:cocoa_beans"},
            {"item": "minecraft:sugar"},
            {"item": "minecraft:gold_nugget"}
        ],
        "result": {"item": "skycasino:anti_tilt_coffee", "count": 2}
    })

    en_add = {
        "item.skycasino.anti_tilt_coffee": "Anti-Tilt Espresso",
        "tooltip.skycasino.coffee_hint": "Cures Withdrawal & Hardcore Tilt, lowers Addiction by 35%.",
        "screen.skycasino.ludik_panel": "Gambler's Den — Loans & Pawn Shop",
        "gui.skycasino.ludik.open": "Ludik / Pawn",
        "gui.skycasino.ludik.addiction": "Addiction Level: %s%%",
        "gui.skycasino.ludik.loan_title": "Micro-Loan:",
        "gui.skycasino.ludik.repay": "Repay Debt",
        "gui.skycasino.ludik.pawn_title": "Withdrawal Pawn Shop (Hand Item):",
        "gui.skycasino.ludik.pawn_btn": "PAWN HELD ITEM!",
        "gui.skycasino.ludik.dodep": "DODEP (+100 if 0 chips)"
    }

    ru_add = {
        "item.skycasino.anti_tilt_coffee": "Кофе «Анти-Тильт»",
        "tooltip.skycasino.coffee_hint": "Снимает Ломку и жёсткий Тильт, снижает Лудоманию на 35%.",
        "screen.skycasino.ludik_panel": "Кабинет Лудика — Займы и Ломбард",
        "gui.skycasino.ludik.open": "Лудик/Ломбард",
        "gui.skycasino.ludik.addiction": "Уровень Лудомании: %s%%",
        "gui.skycasino.ludik.loan_title": "Микрозайм:",
        "gui.skycasino.ludik.repay": "Погасить долг",
        "gui.skycasino.ludik.pawn_title": "Ломбард при Ломке (предмет в руке):",
        "gui.skycasino.ludik.pawn_btn": "СДАТЬ В ЛОМБАРД!",
        "gui.skycasino.ludik.dodep": "ДОДЕП (+100 при 0 фишек)"
    }

    for fname, adds in [("en_us.json", en_add), ("ru_ru.json", ru_add)]:
        p = os.path.join(ASSETS, "lang", fname)
        with open(p, "r", encoding="utf-8") as f:
            data = json.load(f)
        data.update(adds)
        save_json(p, data)

    print("Sky Casino v1.5.0 assets generated successfully!")

if __name__ == "__main__":
    generate_v15()
