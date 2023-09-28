package com.monstrous.lightning3D;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.monstrous.lightning3D.lightning.BranchedLightning;


public class Main extends ApplicationAdapter {
    private PerspectiveCamera cam;
    private Environment environment;
    private ModelBatch modelBatch;
    private Model modelGround;
    private Texture groundTexture;
    private Array<ModelInstance> instances;
    private CameraInputController camController;
    private BranchedLightning lightning;
    private Sound thunder;
    private Vector3 top = new Vector3();
    private Vector3 bottom = new Vector3();
    private Plane groundPlane;



    @Override
    public void create() {
        cam = new PerspectiveCamera(50f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0,2, 0);
        cam.near = 0.1f;
        cam.far = 1000f;
        cam.update();

        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);

        environment = new Environment();
        environment.add(new DirectionalLight().set(0.2f, 0.2f, 0.2f, -0.1f, -0.8f, -0.2f));

        modelBatch = new ModelBatch();

        instances = new Array<>();
        populate();

        lightning = new BranchedLightning();
        thunder = Gdx.audio.newSound(Gdx.files.internal("sound/thunder-25689.mp3"));
    }

    private void populate() {
        ModelBuilder modelBuilder = new ModelBuilder();

        // make a big texture box for the ground
        groundTexture = new Texture(Gdx.files.internal("textures/dirt_2.jpg"), true);
        groundTexture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap);
        groundTexture.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        TextureRegion textureRegion = new TextureRegion(groundTexture);
        int repeats = 20;
        textureRegion.setRegion(0,0,groundTexture.getWidth()*repeats, groundTexture.getHeight()*repeats );

        // create models
        modelGround = modelBuilder.createBox(500f, 1f, 500f,
            new Material(TextureAttribute.createDiffuse(textureRegion)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates );

        // create and position model instances

        instances.add(new ModelInstance(modelGround, 0, -1, 0));	// 'table top' surface
        groundPlane = new Plane(Vector3.Y, 0);
    }



    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        camController.update();
        cam.position.y = 2f;
        cam.up.set(0, 1, 0);
        cam.update();


        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Ray pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
            Intersector.intersectRayPlane(pickRay, groundPlane, bottom);  // get world coordinate where the ground was clicked
            top.set(0,100,0);
            lightning.create(top, bottom);
            thunder.play();
        }

        lightning.update(deltaTime);

        ScreenUtils.clear(Color.BLACK, true);

        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        lightning.render(modelBatch, cam.position);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        modelGround.dispose();
        groundTexture.dispose();
        lightning.dispose();
        thunder.dispose();
    }
}
