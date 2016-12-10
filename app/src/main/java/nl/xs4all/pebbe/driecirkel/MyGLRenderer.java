package nl.xs4all.pebbe.driecirkel;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.os.SystemClock.sleep;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";

    private static float ra = .95f;

    private int width = 100;
    private int height = 100;
    private float ratio = 1.0f;
    private float xmul = ra;
    private float ymul = ra;

    private Axes mAxes;
    private Driehoek mDriehoek;
    private static float Hoek = 0;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // Set the background frame color
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        mAxes = new Axes();
        mDriehoek = new Driehoek();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mAxes.draw();

        mDriehoek.draw(xmul, ymul, (float)Math.sin(Hoek), (float)Math.cos(Hoek));

        Hoek += .04;
        sleep(20);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int w, int h) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, w, h);

        width = w;
        height = h;
        ratio = ((float) width) / (float) height;
        if (ratio > 1.0f) {
            xmul = ra / ratio;
            ymul = ra;
        } else {
            xmul = ra;
            ymul = ra * ratio;
        }
    }

    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

}
