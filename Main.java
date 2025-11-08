import java.util.Random;

public class Main{
    private static final Random rng = new Random();
    // initial extension
    private static final int init_x = 0;
    // initial height
    private static final int init_y = 0;
    // assume length of a time period is 2 seconds
    private static final int dt = 2;
    // assume speed is 5, no acceleration
    private static final int v = 5;
    // maximum number of loops
    private static final int timeOut = 1000;


    public static void fetchBox(int h, int extension){
        //constants
        int delta = 1;
        // sensor variables
        int h_sens = init_y; int e_sens = init_x;
        boolean g_sens = false;
        //prediction and error vars
        int h_next = init_y; int e_next=init_x; boolean g_next = false;
        boolean error = false;
        // to check loop termination
        boolean boxReached = false;
        // initialize all motors off by default
        Motor y_motor = new Motor(MotorStatus.OFF);
        Motor x_motor = new Motor(MotorStatus.OFF);
        Motor grip_motor = new Motor(MotorStatus.OFF);
        // counter to keep track of "time"
        int counter = 0;
        while (counter < timeOut){
            // read sensors. can introduce failures by inputting (delta + 1) instead of delta, for example
            // introducing failures for grip motor is different, maybe by removing the line turning on the grip motor?
            h_sens = getSensorVal(h_next, delta);
            e_sens = getSensorVal(e_next, delta);
            g_sens = grip_motor.status == MotorStatus.ON;
            // handle ERROR
            if (h_sens > h_next + delta || h_sens < h_next - delta
                || e_sens > e_next + delta || e_sens < e_next -delta
                || g_next != g_sens){
                counter++;
                // print all system info before breaking
                System.out.println("-------------------------------");
                System.out.println("status at time : " + 2*counter + " seconds");
                System.out.println("sensors :  h_sens = " + h_sens+ ", e_sens = " + e_sens+ ", g-sens = " + g_sens);
                System.out.println("predictions : h_next = "+ h_next + ", e_next = " + e_next+ ", g_next = "+ g_next);
                System.out.println("motors : y_motor: "+ y_motor.printStatus()+ ", x_motor: "+x_motor.printStatus()+", grip motor: "+ grip_motor.printStatus());
                //safe state, motors off
                x_motor.turnOff();
                y_motor.turnOff();
                System.out.println("ERROR : Notify WMS");
                break;
            }
            // check success condition : height and extension reached and grip motor is on
            if(boxReached && g_next && g_sens){
                counter++;
                System.out.println("--------------------------");
                System.out.println("success at : " + 2*counter+ " seconds");
                break;
            }
            // logic
            if(h_sens < h -delta){
                y_motor.setDirection(Direction.Up);
                y_motor.turnOn();
                // predict
                h_next = h_next + v*dt;
            } else if (h_sens > h + delta) {
                y_motor.setDirection(Direction.Down);
                y_motor.turnOn();
                // predict
                h_next = h_next - v*dt;
            } else if (e_sens < extension - delta){ // h == h_sens +-delta from here onwards
                y_motor.turnOff();
                x_motor.setDirection(Direction.Forward);
                x_motor.turnOn();
                // predict
                e_next = e_next + v*dt;
            } else if ( e_sens > extension +delta){
                x_motor.setDirection(Direction.Backward);
                x_motor.turnOn();
                // predict
                e_next = e_next - v*dt;
            } else { // e_sens == extension +-delta and h_sens == h +-delta. we can pick up the box!!
                y_motor.turnOff();
                x_motor.turnOff();
                //comment out this line to simulate grip motor failure
                grip_motor.turnOn();
                boxReached = true;
                g_next = true; // prediction
            }
            counter++;
            // print system info after every loop
            System.out.println("-------------------------------");
            System.out.println("status at time : " + 2*counter + " seconds");
            System.out.println("sensors :  h_sens = " + h_sens+ ", e_sens = " + e_sens+ ", g-sens = " + g_sens);
            System.out.println("predictions : h_next = "+ h_next + ", e_next = " + e_next+ ", g_next = "+ g_next);
            System.out.println("motors : y_motor: "+ y_motor.printStatus()+ ", x_motor: "+x_motor.printStatus()+", grip motor: "+ grip_motor.printStatus());
        }
    }

    // generate random int in range (val-delta , val+delta). simulates reading sensor values
    public static int getSensorVal(int val, int delta){

        return rng.nextInt(2*delta + 1) + val - delta;
    }

    public static void main(String[] args) {
        fetchBox(40,20);
    }

    public enum MotorStatus{
        ON,
        OFF;
    }

    public enum Direction {
        Up,
        Down,
        Forward,
        Backward
    }
    public static class Motor {
        public MotorStatus status;
        public Direction direction;

        public Motor (MotorStatus status) {
            this.status = status;
        }

        public void turnOn(){
            if (status == MotorStatus.OFF) status = MotorStatus.ON;
        }

        public void turnOff(){
            if (status == MotorStatus.ON) status = MotorStatus.OFF;
        }

        public void setDirection(Direction dir){
            direction = dir;
        }

        public String printStatus(){
            if (status == MotorStatus.ON) return "ON";
            else return "OFF";
        }
    }
}

