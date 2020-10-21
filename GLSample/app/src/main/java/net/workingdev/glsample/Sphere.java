package net.workingdev.glsample;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Sphere {

    private FloatBuffer vertBuffer;
    private ShortBuffer facesBuffer;
    private List<String> vertList;
    private List<String> facesList;
    private Context ctx;
    private final String TAG = getClass().getName();

    private int vertexShader;
    private int fragmentShader;

    private int program;

    public Sphere(Context context) {

        ctx = context;
        vertList = new ArrayList<>();
        facesList = new ArrayList<>();

        loadVertices();
        createBuffers();
        createShaders();
        runProgram();

    }


    private void loadVertices() {

        try {
            Scanner scanner = new Scanner(ctx.getAssets().open("sphere.obj"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("v ")) {
                    vertList.add(line);
                } else if (line.startsWith("f ")) {
                    facesList.add(line);
                }
            }
            scanner.close();
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
        }
    }

    private void createBuffers() {

        // BUFFER FOR VERTICES
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(vertList.size() * 3 * 4);
        buffer1.order(ByteOrder.nativeOrder());
        vertBuffer = buffer1.asFloatBuffer();

        // BUFFER FOR FACES
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(facesList.size() * 3 * 2);
        buffer2.order(ByteOrder.nativeOrder());
        facesBuffer = buffer2.asShortBuffer();

        for (String vertex : vertList) {

            String coords[] = vertex.split(" ");
            float x = Float.parseFloat(coords[1]);
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);
            vertBuffer.put(x);
            vertBuffer.put(y);
            vertBuffer.put(z);

        }

        vertBuffer.position(0);

        for (String face : facesList) {
            String vertexIndices[] = face.split(" ");
            short vertex1 = Short.parseShort(vertexIndices[1]);
            short vertex2 = Short.parseShort(vertexIndices[2]);
            short vertex3 = Short.parseShort(vertexIndices[3]);
            facesBuffer.put((short) (vertex1 - 1));
            facesBuffer.put((short) (vertex2 - 1));
            facesBuffer.put((short) (vertex3 - 1));
        }

        facesBuffer.position(0);
    }

    private void createShaders() {

        try {

            Scanner scannerFrag = new Scanner(ctx.getAssets().open("fragment_shader.txt"));
            Scanner scannerVert = new Scanner(ctx.getAssets().open("vertex_shader.txt"));

            StringBuilder sbFrag = new StringBuilder();
            StringBuilder sbVert = new StringBuilder();

            while (scannerFrag.hasNext()) {
                sbFrag.append(scannerFrag.nextLine());
            }

            while (scannerVert.hasNext()) {
                sbVert.append(scannerVert.nextLine());
            }

            String vertexShaderCode = new String(sbVert.toString());
            String fragmentShaderCode = new String(sbFrag.toString());

            Log.d(TAG, vertexShaderCode);

            vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            GLES20.glShaderSource(vertexShader, vertexShaderCode);

            fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(fragmentShader, fragmentShaderCode);

            GLES20.glCompileShader(vertexShader);
            GLES20.glCompileShader(fragmentShader);
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
        }
    }

    private void runProgram() {

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        GLES20.glUseProgram(program);
    }


    public void draw() {
        int position = GLES20.glGetAttribLocation(program, "position");
        GLES20.glEnableVertexAttribArray(position);
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 3 * 4, vertBuffer);

        float[] projectionMatrix = new float[16];
        float[] viewMatrix = new float[16];
        float[] productMatrix = new float[16];

        Matrix.frustumM(projectionMatrix, 0, -1, 1, -1, 1, 2, 9);

        Matrix.setLookAtM(viewMatrix, 0, 0, 3, -4, 0, 0, 0, 0, 1, 0f);

        Matrix.multiplyMM(productMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        int matrix = GLES20.glGetUniformLocation(program, "matrix");
        GLES20.glUniformMatrix4fv(matrix, 1, false, productMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, facesList.size() * 3, GLES20.GL_UNSIGNED_SHORT, facesBuffer);
        GLES20.glDisableVertexAttribArray(position);

    }
}
