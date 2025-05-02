package com.example.hci_project3.entity;

public class SensorData {
    //Acceleration
    private float x;
    private float y;
    private float z;
    private int accuracy;

    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }
    public void setZ(float z) {
        this.z = z;
    }

    public int getAccuracy() {
        return accuracy;
    }
    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    //Gyroscope
    private float rotx;
    private float roty;
    private float rotz;

    public float getRotx() { return rotx; }
    public void setRotx(float rotx) { this.rotx = rotx; }

    public float getRoty() { return roty; }
    public void setRoty(float roty) { this.roty = roty; }

    public float getRotz() { return rotz; }
    public void setRotz(float rotz) { this.rotz = rotz; }

    public String getText()
    {
        return "Acceleration\nX：" + x + "\nY：" + y + "\nZ：" + z + "\nAccuracy=" + accuracy +
                "\nRot_X: " + rotx + "\nRot_Y: " + roty + "\nRot_z: " + rotz;
    }
}
