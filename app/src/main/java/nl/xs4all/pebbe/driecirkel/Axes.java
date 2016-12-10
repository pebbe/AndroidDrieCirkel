package nl.xs4all.pebbe.driecirkel;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Axes {

    private final String vertexShaderCode = ""+
            "attribute vec2 vPosition;" +
            "void main() {" +
            "    gl_Position = vec4(vPosition, 0.0, 1.0);" +
            "}";

    private final String fragmentShaderCode = ""+
            "void main() {" +
            "    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);" +
            "}";

    private final FloatBuffer vertexBuffer;
    private final int mProgram;
    private int mPositionHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;
    static float axesCoords[] = {
            -1.0f, 0.0f, 1.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 1.0f
    };
    private final int vertexCount = axesCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public Axes() {
        ByteBuffer bb = ByteBuffer.allocateDirect(axesCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(axesCoords);
        vertexBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

    }

    public void draw() {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare thecoordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Draw
        GLES20.glLineWidth(5);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}
