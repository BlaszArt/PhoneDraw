package pl.edu.pw.eiti.wpam.ablaszcz.phonedraw;

class Model {

    private boolean draw3D;
    private final float SCALE = 1000f;
    private final int FACTOR_DATA = 128;

    private float last_ts = 0;
    private float[] velocity = new float[3];
    private float[] acc = new float[3];
    private float[] move = new float[2];

    Model(Boolean draw3D){
        this.draw3D = draw3D;
    }

    float[] calc(float[] values, float ts) {

        // convert from microsecunds to seconds
        float cur_ts = ts / 1000000000.0f;

        if (last_ts == 0) {
            last_ts = cur_ts;
            return move;
        }
        float dt = cur_ts - last_ts;
        last_ts = cur_ts;

        if (draw3D){
            return this.integrate3D(this.scale(values), dt);
        }
        else {
            return this.integrate2D(this.scale(values), dt);
        }
    }

    private float[] scale(float[] values) {
        float[] scaled = new float[values.length];
        for(int i = 0; i < values.length; i++) {
            scaled[i] = Math.round(values[i] * SCALE) / SCALE;
        }
        return scaled;
    }

    private float[] integrate2D(float[] acc, float dt) {
        for (int i = 0; i < move.length; i++) {
            this.acc[i] += acc[i];
            this.velocity[i] += this.acc[i] * dt;
            System.out.println(this.metersToPixel(this.velocity[i] * dt) / FACTOR_DATA);
            this.move[i] += this.metersToPixel(this.velocity[i] * dt) / FACTOR_DATA;
        }
        System.out.println(move[0]);
        System.out.println(move[1]);

        return move;
    }

    private float[] integrate3D(float[] acc, float dt) {
        for (int i = 0; i < velocity.length; i++) {
            this.acc[i] += acc[i];
            this.velocity[i] += this.acc[i] * dt;
        }

        //float vel_x = new Float(this.velocity[0] - Math.sqrt(2.0) / 2f * this.velocity[2]);
        //float vel_y = new Float(this.velocity[1] + Math.sqrt(2.0) / 2f * this.velocity[2]);

        float vel_x = new Float(this.velocity[0] - Math.sqrt(3.0) / 2f * this.velocity[2]);
        float vel_y = new Float(this.velocity[1] + 1 / 2f * this.velocity[2]);

        this.move[0] += this.metersToPixel(vel_x * dt) / FACTOR_DATA;
        this.move[1] += this.metersToPixel(vel_y * dt) / FACTOR_DATA;

        System.out.println(move[0]);
        System.out.println(move[1]);

        return move;
    }
    private float metersToPixel(float x) {
        return x * 3779.5275590551f;
    }

    void reset(){
        for(int i = 0; i < velocity.length; i++){
            acc[i] = 0;
            velocity[i] = 0;

        }
        last_ts = 0;
    }

    void setStartPoint(float start_x, float start_y){
        System.out.println("SET START POS");
        System.out.println(start_x);
        System.out.println(start_y);
        this.move[0] = start_x;
        this.move[1] = start_y;
    }

    void setDraw3D() {
        draw3D = true;
    }
}
