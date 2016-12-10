package nl.xs4all.pebbe.driecirkel;

import android.graphics.Color;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Driehoek {

    private final String TAG = "DRIEHOEK";

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colorBuffer;
    private final FloatBuffer circVertexBuffer;
    private final FloatBuffer circColorBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mXmulHandle;
    private int mYmulHandle;
    private int mSinHandle;
    private int mCosHandle;

    private final String vertexShaderCode = "" +
            "uniform float xmul;" +
            "uniform float ymul;" +
            "uniform float sin;" +
            "uniform float cos;" +
            "attribute vec3 vertexColor;" +
            "attribute vec2 position;" +
            "varying vec3 color;" +
            "void main() {" +
            "    gl_Position = vec4(xmul * (cos*position[0] + sin*position[1]), ymul * (sin*position[0] - cos*position[1]), 0.0, 1.0);" +
            "    color = vertexColor;" +
            "}";

    private final String fragmentShaderCode = "" +
            "precision mediump float;" +
            "varying vec3 color;" +
            "void main() {" +
            "    gl_FragColor = vec4(color, 1.0);" +
            "}";

    // driehoek

    static final int COORDS_PER_VERTEX = 2;
    static float triCoords[] = {
            0.0f, 1.0f,
            0.866f, -0.5f,
            -0.866f, -0.5f
    };
    private final int vertexCount = triCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static final int COLORS_PER_VERTEX = 3;
    static float triColors[] = {
            1, 0, 0,
            0, 1, 0,
            0, 0, 1
    };
    private final int colorCount = triColors.length / COLORS_PER_VERTEX;
    private final int colorStride = COLORS_PER_VERTEX * 4; // 4 bytes per vertex

    // cirkel

    private int circCount;
    static final int circVertexCount = 127;
    static final int CIRC_COORDS_PER_VERTEX = 2;
    static float circCoords[] = new float[circVertexCount * CIRC_COORDS_PER_VERTEX];
    static int circVertexStride = CIRC_COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static final int circColorCount = circVertexCount;
    static final int CIRC_COLORS_PER_VERTEX = 3;
    static float circColors[] = new float[circColorCount * CIRC_COLORS_PER_VERTEX];
    static int circColorStride = CIRC_COLORS_PER_VERTEX * 4; // 4 bytes per vertex


    public Driehoek() {

        // driehoek

        ByteBuffer bb = ByteBuffer.allocateDirect(triCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triCoords);
        vertexBuffer.position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(triColors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(triColors);
        colorBuffer.position(0);

        // cirkel

        int p = 0;
        float hsv[] = new float[3];
        hsv[1] = 1;
        hsv[2] = 1;
        for (float i = 0; i < 2 * (float) Math.PI; i += .1f) { // step: minimaal .05f

            hsv[0] = i / (float)Math.PI * 180;
            int c = Color.HSVToColor(hsv);

            circColors[3 * p] = Color.red(c) / 255.0f;
            circColors[3 * p + 1] = Color.green(c) / 255.0f;
            circColors[3 * p + 2] = Color.blue(c) / 255.0f;

            circCoords[2 * p] = (float) Math.sin(i);
            circCoords[2 * p + 1] = (float) Math.cos(i);
            p++;
        }
        circCount = p;

        ByteBuffer cbb = ByteBuffer.allocateDirect(circCoords.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        circVertexBuffer = cbb.asFloatBuffer();
        circVertexBuffer.put(circCoords);
        circVertexBuffer.position(0);

        ByteBuffer ccb = ByteBuffer.allocateDirect(circColors.length * 4);
        ccb.order(ByteOrder.nativeOrder());
        circColorBuffer = ccb.asFloatBuffer();
        circColorBuffer.put(circColors);
        circColorBuffer.position(0);

        // driehoek en cirkel

        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables


    }

    public void draw(float xmul, float ymul, float sin, float cos) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "vertexColor");
        mXmulHandle = GLES20.glGetUniformLocation(mProgram, "xmul");
        mYmulHandle = GLES20.glGetUniformLocation(mProgram, "ymul");
        mSinHandle = GLES20.glGetUniformLocation(mProgram, "sin");
        mCosHandle = GLES20.glGetUniformLocation(mProgram, "cos");

        // Enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glUniform1f(mXmulHandle, xmul);
        GLES20.glUniform1f(mYmulHandle, ymul);
        GLES20.glUniform1f(mSinHandle, sin);
        GLES20.glUniform1f(mCosHandle, cos);

        //
        // driehoek
        //

        // Prepare thecoordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Prepare color data
        GLES20.glVertexAttribPointer(
                mColorHandle, COLORS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                colorStride, colorBuffer);

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        //
        // cirkel
        //

        // Prepare thecoordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, CIRC_COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                circVertexStride, circVertexBuffer);

        // Prepare color data
        GLES20.glVertexAttribPointer(
                mColorHandle, CIRC_COLORS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                circColorStride, circColorBuffer);

        // Draw
        GLES20.glLineWidth(30);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, circCount);

        //
        // clean up
        //

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
