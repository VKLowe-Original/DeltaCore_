package com.deo.flapd.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.concurrent.TimeUnit;

import static com.deo.flapd.utils.DUtils.getInteger;
import static com.deo.flapd.utils.DUtils.getItemCodeNameByName;
import static com.deo.flapd.utils.DUtils.getLong;
import static com.deo.flapd.utils.DUtils.getRandomInRange;
import static com.deo.flapd.utils.DUtils.getString;
import static com.deo.flapd.utils.DUtils.putLong;
import static com.deo.flapd.utils.DUtils.putString;

public class ItemSlotManager {

    private BitmapFont font;
    private Table table, inventory;
    Group holderGroup;
    private Skin slotSkin;
    private TextureAtlas items;
    private ScrollPane scrollPane;
    private Stage stage;
    private AssetManager assetManager;
    private JsonValue treeJson = new JsonReader().parse(Gdx.files.internal("shop/tree.json"));

    ItemSlotManager(AssetManager assetManager){

        slotSkin = new Skin();
        slotSkin.addRegions((TextureAtlas)assetManager.get("shop/workshop.atlas"));

        this.assetManager = assetManager;

        items = assetManager.get("items/items.atlas");

        font = assetManager.get("fonts/font2(old).fnt");
        font.getData().setScale(0.2f);

        table = new Table();
        holderGroup = new Group();
        inventory = new Table();

        inventory.setBackground(new TextureRegionDrawable(new Texture("buttonPauseBlank_disabled.png")));

        scrollPane = new ScrollPane(table);

        long lastGenerationTime = getLong("lastGenTime");
        if(TimeUtils.timeSinceMillis(lastGenerationTime)>18000000){
            putLong("lastGenTime", TimeUtils.millis());
            addInfo();
            generateSlots();
        }else{
            addInfo();
            loadSlots();
        }

        holderGroup.addActor(scrollPane);
        holderGroup.addActor(inventory);
    }

    private void generateSlots(){
        Array<String> itemsToAdd = new Array<>();
        Array<Integer> quantities= new Array<>();

        for(int i = 0; i<treeJson.size; i++){
            if(treeJson.get(i).get("category").asString().equals("recepies")){
                itemsToAdd.add(treeJson.get(i).name);
            }
        }

        boolean nextRow = false;
        Array<String> addedItems = new Array<>();
        int slotQuantity = getRandomInRange(itemsToAdd.size/4, itemsToAdd.size/2);
        for(int i = 0; i<slotQuantity; i++){
            int index = getRandomInRange(0, itemsToAdd.size-1);
            int quantity = MathUtils.clamp(getRandomInRange(5, 15)-getComplexity(itemsToAdd.get(index)), 1, 15);
            addedItems.add(itemsToAdd.get(index));
            quantities.add(quantity);
            addSlot(itemsToAdd.get(index), quantity, nextRow);
            itemsToAdd.removeIndex(index);
            nextRow = !nextRow;
        }

        putString("savedSlots", "{\"slots\":" + addedItems.toString() + ","+ "\"productQuantities\":" + quantities.toString() + "}");
    }

    private void loadSlots(){
        JsonValue slotsJson = new JsonReader().parse(getString("savedSlots"));
        int[] productQuantities = slotsJson.get("productQuantities").asIntArray();
        String[] slotNames = slotsJson.get("slots").asStringArray();
        boolean nextRow = false;
        for(int i = 0; i<productQuantities.length; i++){
            addSlot(slotNames[i], productQuantities[i], nextRow);
            nextRow = !nextRow;
        }
    }

    private void addSlot(final String result, final int availableQuantity, boolean nextRow){

        ImageButton.ImageButtonStyle slotStyle;
        ImageButton.ImageButtonStyle lockedSlotStyle;

        lockedSlotStyle = new ImageButton.ImageButtonStyle();
        lockedSlotStyle.up = slotSkin.getDrawable("slot_disabled");
        lockedSlotStyle.down = slotSkin.getDrawable("slot_disabled_down");
        lockedSlotStyle.over = slotSkin.getDrawable("slot_disabled_over");

        slotStyle = new ImageButton.ImageButtonStyle();
        slotStyle.up = slotSkin.getDrawable("slot");
        slotStyle.over = slotSkin.getDrawable("slot_over");
        slotStyle.down = slotSkin.getDrawable("slot_enabled");

        Image imageUp_scaled = new Image(this.items.findRegion(getItemCodeNameByName(result)));
        Image imageOver_scaled = new Image(this.items.findRegion("over_"+getItemCodeNameByName(result)));
        Image imageDisabled_scaled = new Image(this.items.findRegion("disabled_"+getItemCodeNameByName(result)));
        Image imageDown_scaled = new Image(this.items.findRegion("enabled_"+getItemCodeNameByName(result)));

        float heightBefore = imageUp_scaled.getHeight();
        float widthBefore = imageUp_scaled.getWidth();
        float height = 64;
        float width = (height/heightBefore)*widthBefore;

        slotStyle.imageUp = imageUp_scaled.getDrawable();
        slotStyle.imageOver = imageOver_scaled.getDrawable();
        slotStyle.imageDown = imageDown_scaled.getDrawable();

        lockedSlotStyle.imageUp = imageDisabled_scaled.getDrawable();

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;

        ImageButton slot = new ImageButton(slotStyle){
            @Override
            public void draw(Batch batch, float parentAlpha) {
                try {
                    super.draw(batch, parentAlpha);
                } catch (Exception e) {
                    throw new NullPointerException("error drawing "+getItemCodeNameByName(result) + "\n" +items.findRegion(getItemCodeNameByName(result))+ "\n" +items.findRegion("over_"+getItemCodeNameByName(result))+ "\n" +items.findRegion("enabled_"+getItemCodeNameByName(result))+ "\n" +items.findRegion("disabled_"+getItemCodeNameByName(result)));
                }
            }
        };

        Label text = new Label(result, labelStyle);
        text.setFontScale(0.28f, 0.3f);
        text.setColor(Color.YELLOW);

        Label quantity = new Label(""+availableQuantity, labelStyle);
        quantity.setFontScale(0.4f);
        quantity.setColor(Color.YELLOW);
        quantity.setBounds(133, 88, 50, 20);
        quantity.setAlignment(Align.right);

        slot.getImageCell().size(width, height).padBottom(5).row();
        slot.add(text).padRight(10).padLeft(10).padTop(10);
        slot.addActor(quantity);

        slot.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new PurchaseDialogue(assetManager, stage, result, availableQuantity, ItemSlotManager.this);
            }
        });

        if(nextRow) {
            table.add(slot).padBottom(5).padTop(5).padLeft(5).size(205, 120);
            table.row();
        }else{
            table.add(slot).padBottom(5).padTop(5).padRight(5).size(205, 120);
        }
    }

    private void addInfo(){
        Label.LabelStyle yellowLabelStyle = new Label.LabelStyle();
        yellowLabelStyle.font = font;
        yellowLabelStyle.fontColor = Color.YELLOW;

        Table holder = new Table();
        Label uraniumCells_text = new Label(""+getInteger("money"), yellowLabelStyle);
        Label cogs_text = new Label(""+getInteger("cogs"), yellowLabelStyle);
        uraniumCells_text.setFontScale(0.5f);
        cogs_text.setFontScale(0.5f);

        Image uraniumCell = new Image((Texture)assetManager.get("uraniumCell.png"));
        uraniumCell.setScaling(Scaling.fit);
        holder.add(uraniumCell).size(30, 30);
        holder.add(uraniumCells_text).padLeft(5);

        Table holder2 = new Table();
        holder2.add(new Image(assetManager.get("bonuses.atlas", TextureAtlas.class).findRegion("bonus_part"))).size(30, 30);
        holder2.add(cogs_text).padLeft(5);
        inventory.align(Align.left);
        final long[] nextUpdateTime = {getLong("lastGenTime") + 18000000};
        long lastReset = nextUpdateTime[0] - TimeUtils.millis();
        int hours = (int)TimeUnit.MILLISECONDS.toHours(lastReset);
        int minutes = (int)TimeUnit.MILLISECONDS.toMinutes(lastReset) - hours * 60;
        int seconds = (int)TimeUnit.MILLISECONDS.toSeconds(lastReset) - hours * 3600 - minutes * 60;
        Label resetTime = new Label("Reset in: "+hours+"h "+minutes+"m "+seconds+"s", yellowLabelStyle){
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                long nextReset = nextUpdateTime[0] - TimeUtils.millis();
                int hours = (int)TimeUnit.MILLISECONDS.toHours(nextReset);
                int minutes = (int)TimeUnit.MILLISECONDS.toMinutes(nextReset) - hours * 60;
                int seconds = (int)TimeUnit.MILLISECONDS.toSeconds(nextReset) - hours * 3600 - minutes * 60;
                this.setText("||Reset in: "+hours+"h "+minutes+"m "+seconds+"s||");
                if(hours <= 0 && minutes <= 0 && seconds <= 0){
                    putLong("lastGenTime", TimeUtils.millis());
                    nextUpdateTime[0] = TimeUtils.millis();
                    table.clearChildren();
                    generateSlots();
                }
            }
        };
        resetTime.setFontScale(0.5f);
        resetTime.setColor(Color.ORANGE);
        Table moneyAndCogs = new Table();
        moneyAndCogs.add(holder).align(Align.left).padLeft(10).padBottom(5).row();
        moneyAndCogs.add(holder2).align(Align.left).padLeft(10);

        inventory.add(moneyAndCogs);
        inventory.add(resetTime).padLeft(10).align(Align.center);
    }

    void attach(Stage stage){
        stage.addActor(holderGroup);
        this.stage = stage;
    }

    public void setBounds(float x, float y, float width, float height) {
        height-=85;
        inventory.setBounds(x, y+height, width, 85);
        scrollPane.setBounds(x, y, width, height);
    }

    void update(){
        inventory.clearChildren();
        addInfo();
        table.clearChildren();
        loadSlots();
    }

    private int getComplexity(String result){
        int complexity = 0;
        if(treeJson.get(result) == null) throw new IllegalArgumentException("no item declared with name "+result);
        JsonValue price = treeJson.get(result).get("price");
        if(price.asString().equals("auto")){
            String[] items = treeJson.get(result).get("items").asStringArray();
            for(int i = 0; i<items.length; i++){
                int buffer = getComplexity(items[i]);
                complexity += buffer + 1;
            }
        }
        complexity = MathUtils.clamp((int)(Math.ceil(complexity/2f)-1)*4, 0, 15);
        return complexity;
    }
}
