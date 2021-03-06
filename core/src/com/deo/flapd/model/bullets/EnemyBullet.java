package com.deo.flapd.model.bullets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.deo.flapd.model.Bullet;
import com.deo.flapd.model.Meteorite;
import com.deo.flapd.model.SpaceShip;

public class EnemyBullet {

    public Sprite bullet;
    public BulletData data;
    public boolean isDead = false;
    public boolean queuedForDeletion = false;
    private boolean explosionFinished = false;

    public EnemyBullet(AssetManager assetManager, BulletData bulletData) {
        data = bulletData;
        bullet = new Sprite((Texture) assetManager.get(bulletData.texture));
        bullet.setPosition(bulletData.x, bulletData.y);
        bullet.setRotation(bulletData.angle);
        bullet.setSize(bulletData.width, bulletData.height);
        bullet.setOrigin(bulletData.width / 2, bulletData.height / 2);

        bulletData.explosionParticleEffect = new ParticleEffect();
        bulletData.explosionParticleEffect.load(Gdx.files.internal(bulletData.explosion), Gdx.files.internal("particles"));
        bulletData.explosionParticleEffect.scaleEffect(bulletData.explosionScale);

        bulletData.trailParticleEffect = new ParticleEffect();
        bulletData.trailParticleEffect.load(Gdx.files.internal(bulletData.trail), Gdx.files.internal("particles"));
        bulletData.trailParticleEffect.scaleEffect(bulletData.trailScale);
        bulletData.trailParticleEffect.start();
    }

    public void draw(SpriteBatch batch) {
        if (!isDead) {
            bullet.draw(batch);
            data.trailParticleEffect.draw(batch);
        } else {
            data.explosionParticleEffect.draw(batch);
        }
    }

    public void update(float delta) {
        for (int i = 0; i < Bullet.bullets.size; i++) {
            if (Bullet.bullets.get(i).overlaps(bullet.getBoundingRectangle())) {
                explode();
                Bullet.removeBullet(i, true);
            }
        }
        if (bullet.getBoundingRectangle().overlaps(SpaceShip.bounds.getBoundingRectangle())) {
            explode();
            SpaceShip.takeDamage(data.damage);
        }
        for (int i = 0; i < Meteorite.meteorites.size; i++) {
            if (Meteorite.meteorites.get(i).overlaps(bullet.getBoundingRectangle())) {
                Meteorite.healths.set(i, Meteorite.healths.get(i) - data.damage);
                explode();
            }
        }
        if (!isDead) {
            data.trailParticleEffect.update(delta);
            data.x -= MathUtils.cosDeg(bullet.getRotation()) * data.speed * delta;
            data.y -= MathUtils.sinDeg(bullet.getRotation()) * data.speed * delta;
            bullet.setPosition(data.x, data.y);
            data.trailParticleEffect.setPosition(data.x + data.width / 2, data.y + data.height / 2);

            if (data.x < -data.width - data.trailParticleEffect.getBoundingBox().getWidth() - 20) {
                isDead = true;
                explosionFinished = true;
            }
        } else {
            data.explosionParticleEffect.update(delta);
        }
        queuedForDeletion = (data.explosionParticleEffect.isComplete() || explosionFinished) && isDead;
    }

    public void dispose() {
        data.explosionParticleEffect.dispose();
        data.trailParticleEffect.dispose();
    }

    private void explode() {
        data.trailParticleEffect.dispose();
        data.explosionParticleEffect.setPosition(data.x + data.width / 2, data.y + data.height / 2);
        data.explosionParticleEffect.start();
        bullet.setPosition(-100, -100);
        isDead = true;
    }

}
