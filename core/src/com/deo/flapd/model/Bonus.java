package com.deo.flapd.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.deo.flapd.control.GameLogic;
import com.deo.flapd.view.GameUi;

import java.util.Random;

public class Bonus {

    private Polygon bounds;
    public static Array<Rectangle> bonuses;
    public static Array<Integer> types;
    public static Array<Float> anglesY;
    private static Array <ParticleEffect> explosions;
    private Sprite bonus_health, bonus_shield, bonus_part, bonus_bullets;

    private float width, height;

    private BitmapFont font_text;

    private float uiScale;

    private Preferences prefs;

    private Texture bonus_bullets_t;
    private Random random;

    public Bonus(AssetManager assetManager, float width, float height, Polygon shipBounds){
        bounds = shipBounds;

        random = new Random();

        prefs = Gdx.app.getPreferences("Preferences");

        bonus_health = new Sprite((Texture)assetManager.get("bonus_health.png"));
        bonus_shield = new Sprite((Texture)assetManager.get("bonus_shield.png"));
        bonus_part = new Sprite((Texture)assetManager.get("bonus_part.png"));
        bonus_bullets = new Sprite((Texture)assetManager.get("bonus_bullets.png"));

        bonus_bullets_t = assetManager.get("bonus_bullets.png");

        this.width = width;
        this.height = height;

        bonuses = new Array<>();
        types = new Array<>();
        explosions = new Array<>();
        anglesY = new Array<>();

        font_text = assetManager.get("fonts/font2.fnt");

        uiScale = prefs.getFloat("ui");
    }

    public void Spawn(int type, float scale, Rectangle enemy) {

            Rectangle bonus = new Rectangle();

            bonus.x = enemy.getX()+enemy.width/2-width/2;
            bonus.y = enemy.getY()+enemy.height/2-height/2;

            bonus.setSize(width*scale, height*scale);

            bonuses.add(bonus);
            types.add(type);
            anglesY.add(random.nextFloat()*2-1);
    }

    public void Spawn(int type, float scale, Circle enemy) {

        Rectangle bonus = new Rectangle();

        bonus.x = enemy.x+enemy.radius-width/2;
        bonus.y = enemy.y+enemy.radius-height/2;

        bonus.setSize(width*scale, height*scale);

        bonuses.add(bonus);
        types.add(type);
        anglesY.add(random.nextFloat()*2-1);
    }

    public void draw(SpriteBatch batch, boolean is_paused) {

        for (int i = 0; i < bonuses.size; i ++) {

            Rectangle bonus = bonuses.get(i);
            Integer type = types.get(i);

            float angleY = anglesY.get(i);

            switch (type){
                case(1):
                    this.bonus_shield.setPosition(bonus.x, bonus.y);
                    this.bonus_shield.setSize(bonus.width, bonus.height);
                    this.bonus_shield.setOrigin(bonus.width / 2f, bonus.height / 2f);
                    this.bonus_shield.draw(batch);
                    break;
                case(2):
                    this.bonus_health.setPosition(bonus.x, bonus.y);
                    this.bonus_health.setSize(bonus.width, bonus.height);
                    this.bonus_health.setOrigin(bonus.width / 2f, bonus.height / 2f);
                    this.bonus_health.draw(batch);
                    break;
                case(3):
                    this.bonus_bullets.setPosition(bonus.x, bonus.y);
                    this.bonus_bullets.setSize(bonus.width, bonus.height);
                    this.bonus_bullets.setOrigin(bonus.width / 2f, bonus.height / 2f);
                    this.bonus_bullets.draw(batch);
                    break;
                case(4):
                    this.bonus_part.setPosition(bonus.x, bonus.y);
                    this.bonus_part.setSize(bonus.width, bonus.height);
                    this.bonus_part.setOrigin(bonus.width / 2f, bonus.height / 2f);
                    this.bonus_part.draw(batch);
                    break;

                    default:break;
            }

            if (!is_paused){
                bonus.y -= angleY * 15 * Gdx.graphics.getDeltaTime();
                bonus.x -= 50 * Gdx.graphics.getDeltaTime();

                if(bonus.y < -height || bonus.y > 480 || bonus.x < -height || bonus.x > 800){
                    removeBonus(i, false);
                }

                if(bonus.overlaps(bounds.getBoundingRectangle())){
                    if(type == 1){
                        removeBonus(i, true);
                        if(GameUi.Shield<=80) {
                            GameUi.Shield += 50;
                        }else{
                            GameUi.Shield = 100;
                        }
                    }
                    if(type == 2){
                        removeBonus(i, true);
                        if(GameUi.Health<=80) {
                            GameUi.Health += 20;
                        }else{
                            GameUi.Health = 100;
                        }
                    }
                    if(type == 3){
                        removeBonus(i, true);
                        GameLogic.bonuses_collected += 1;
                    }
                    if(type == 4){
                        removeBonus(i, true);
                    }
                }
            }
        }
        if(GameLogic.bonuses_collected > 0){
            font_text.setColor(Color.WHITE);
            font_text.getData().setScale(0.3f*uiScale);
            font_text.draw(batch, "X" + GameLogic.bonuses_collected, 475-321*(uiScale-1), 425-55*(uiScale-1), 24*uiScale, 1,false);
            batch.draw(bonus_bullets_t, 461-333*(uiScale-1), 475-50*uiScale, 50*uiScale, 50*uiScale);
            font_text.setColor(Color.BLACK);
        }
        for(int i3 = 0; i3 < explosions.size; i3 ++){
            explosions.get(i3).draw(batch);
            if(!is_paused) {
                explosions.get(i3).update(Gdx.graphics.getDeltaTime());
            }else{
                explosions.get(i3).update(0);
            }
            if(explosions.get(i3).isComplete()){
                explosions.get(i3).dispose();
                explosions.removeIndex(i3);
            }
        }
    }

    public void dispose(){
        for (int i = 0; i < bonuses.size; i ++) {
            bonuses.removeIndex(i);
            types.removeIndex(i);
            anglesY.removeIndex(i);
        }
        for(int i3 = 0; i3 < explosions.size; i3 ++){
            explosions.get(i3).dispose();
            explosions.removeIndex(i3);
        }
        font_text.dispose();
    }

    public void removeBonus(int i, boolean explode){
        if(explode){
            ParticleEffect explosionEffect = new ParticleEffect();
            switch (types.get(i)){
                case (1): explosionEffect.load(Gdx.files.internal("particles/explosion4.p"), Gdx.files.internal("particles")); break;
                case (2): explosionEffect.load(Gdx.files.internal("particles/explosion4_1.p"), Gdx.files.internal("particles")); break;
                case (3): explosionEffect.load(Gdx.files.internal("particles/explosion4_2.p"), Gdx.files.internal("particles")); break;
                case (4): explosionEffect.load(Gdx.files.internal("particles/explosion4_3.p"), Gdx.files.internal("particles")); break;
            }
            explosionEffect.setPosition(bonuses.get(i).x + bonuses.get(i).width/2, bonuses.get(i).y + bonuses.get(i).height/2);
            explosionEffect.start();
            explosions.add(explosionEffect);
        }
        bonuses.removeIndex(i);
        types.removeIndex(i);
        anglesY.removeIndex(i);
    }

}