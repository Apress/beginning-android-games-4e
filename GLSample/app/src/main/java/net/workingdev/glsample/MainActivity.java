package net.workingdev.glsample;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glView;
    private Sphere sphere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glView = findViewById(R.id.gl_view);

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo ci = am.getDeviceConfigurationInfo();

        boolean isES2Supported = ci.reqGlEsVersion > 0x20000;

        if (isES2Supported) {

            glView.setEGLContextClientVersion(2);
            glView.setRenderer(new GLSurfaceView.Renderer() {
                @Override
                public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
                    glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    sphere = new Sphere(getApplicationContext());
                }

                @Override
                public void onSurfaceChanged(GL10 gl10, int width, int height) {
                    GLES20.glViewport(0, 0, width, height);
                }

                @Override
                public void onDrawFrame(GL10 gl10) {
                    sphere.draw();
                }
            });
        } else {

        }
    }
}
