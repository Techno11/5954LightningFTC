package TestCode;

import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.hardware.adafruit.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;
import org.firstinspires.ftc.teamcode.BallManagementSystem;
import org.firstinspires.ftc.teamcode.BallShooterSystem;
import org.firstinspires.ftc.teamcode.ButtonPushSystem;
import org.firstinspires.ftc.teamcode.LargeBallLiftSystem;

import java.util.Locale;

//This code will drive to the center vortex and shoot the balls
//It then tries to get to the beacons and press them
//Modified to work with Adafruit IMU
@Autonomous(name="Red Beacon: Auto Drive", group="5954")
@Disabled
public class LightbotRedAutoDriveByGyro_Linear extends LightningAutonomousBaseOpmode{
    @Override
    public void runOpMode() throws InterruptedException {

        initHardware(); //Performs all off the initialization+gyro calibration in the base class

        //For Red Team Drive 8 inches more, then turn left
        gyroDrive(DRIVE_SPEED, 44+8, 0.0);  // Drive FWD
        Thread.sleep(500);

        BallShooter.ShootBall();
        Thread.sleep(500);

        // keep looping while we have time remaining.
        holdTimer.reset();
        while (opModeIsActive() && (holdTimer.time() < 4)) {  //2 seconds
            BallManagement.Lift(true, false);
            idle();
        }

        BallManagement.Lift(false, false);
        BallShooter.StopShooter();

        gyroDrive(DRIVE_SPEED, 44+4, 0.0);  // Drive FWD
        Thread.sleep(750);

        gyroTurn(TURN_SPEED, -45.0);        // Turn CW to 45 Degrees
        Thread.sleep(750);

        //Drive into wall
        Motors.leftMotor.setPower(.375);
        Motors.rightMotor.setPower(.375);
        Motors.leftMotor2.setPower(.375);
        Motors.rightMotor2.setPower(.375);
        Thread.sleep(1500);
        Motors.leftMotor.setPower(0);
        Motors.rightMotor.setPower(0);
        Motors.leftMotor2.setPower(0);
        Motors.rightMotor2.setPower(0);

        gyroDrive(DRIVE_SPEED, -3, 0.0);  // Drive FWD
        Thread.sleep(750);
        gyroTurn(TURN_SPEED, 120.0);        // Turn CW to 45 Degrees

        //Push out roller to maintain distance from wall
        ButtonPush.FrontPushOut();
        //Try stopping the gyro to avoid any conflicts
        //on the i2c bus with the color sensors
        imu.stopAccelerationIntegration();
        while (ButtonPush.csPushR.red() > 200)
        {
            idle();
        }
        Thread.sleep(100);

        Motors.leftMotor.setPower(.25);
        Motors.rightMotor.setPower(.25);
        Motors.leftMotor2.setPower(.25);
        Motors.rightMotor2.setPower(.25);

        //Red Team
        telemetry.addData("BColor", "%3d:%3d", ButtonPush.csPushR.red(), ButtonPush.csPushR.blue());
        while(ButtonPush.csPushR.red() < 10)
        {
            //telemetry.addData("FColor", "%3d:%3d", csChasis.red(), csChasis.green() , csChasis.blue());
            telemetry.addData("BColor", "%3d:%3d", ButtonPush.csPushR.red(), ButtonPush.csPushR.blue());
            telemetry.update();
            //Thread.sleep(2);
            idle();
        }

        Motors.leftMotor.setPower(0);
        Motors.rightMotor.setPower(0);
        Motors.leftMotor2.setPower(0);
        Motors.rightMotor2.setPower(0);

        //Push the beacon button
        ButtonPush.RearPushOut();

        Thread.sleep(1500);
        //Retract the servos
        ButtonPush.RearPushIn();
        ButtonPush.FrontPushIn();
        Thread.sleep(500);

        telemetry.addData("Path", "Complete");
        telemetry.update();
    }
}


//TODO: Restore below if LightningAutonomousBaseOpmode doesn't work
//public class LightbotRedAutoDriveByGyro_Linear extends LinearOpMode {
//
//    /* Declare OpMode members. */
//    LightningDrive robot   = new LightningDrive();   // Use Lightbot's hardware
//    //ModernRobticsI2cGyro   gyro    = null;                    // Additional Gyro device
//
//    BallManagementSystem BallManagement = new BallManagementSystem();
//    BallShooterSystem BallShooter = new BallShooterSystem();
//    ButtonPushSystem ButtonPush = new ButtonPushSystem();
//    //DriveSystemBase DriveSystem = new DriveSystemBase();
//    LargeBallLiftSystem BallLift = new LargeBallLiftSystem();
//    public ColorSensor csChasis = null;
//
//    //public LightningColorSensor testColorSensor = null;
//
//    // The Adafruit IMU sensor object
//    BNO055IMU imu;
//
//    // State used for updating telemetry
//    Orientation angles;
//    Orientation newAngles;
//
//    Acceleration gravity;
//    //Output counts per revolution of Output Shaft
//    static final double     COUNTS_PER_MOTOR_REV    = 1120; //(cpr): 1120 (280 rises of Channel A) // eg: TETRIX Motor Encoder: 1440
//    static final double     DRIVE_GEAR_REDUCTION    = 1;   //2.0 ;     // This is < 1.0 if geared UP
//    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
//    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
//            (WHEEL_DIAMETER_INCHES * 3.1415);
//
//    // These constants define the desired driving/control characteristics
//    // The can/should be tweaked to suite the specific Motors drive train.
//    static final double     DRIVE_SPEED             = 0.7;     // Nominal speed for better accuracy.
//    static final double     TURN_SPEED              = 1; //0.5;     // Nominal half speed for better accuracy.
//
//    static final double     HEADING_THRESHOLD       = 2;  //Original: 1 ;      // As tight as we can make it with an integer gyro
//    static final double     P_TURN_COEFF            = 0.005; //Original: .1    // Larger is more responsive, but also less stable
//    static final double     P_DRIVE_COEFF           = 0.03; //Original: 0.15;     // Larger is more responsive, but also less stable
//
//    private void resetIMU_Position_Integration()
//    {
//        imu.stopAccelerationIntegration();
//        // Start the logging of measured acceleration
//        imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);
//    }
//
//    @Override
//    public void runOpMode() throws InterruptedException {
//
//        /*
//         * Initialize the standard drive system variables.
//         * The init() method of the hardware class does most of the work here
//         */
//        robot.init(hardwareMap);
//        //gyro = (ModernRoboticsI2cGyro)hardwareMap.gyroSensor.get("gyro");
//
//        //DriveSystem.init(hardwareMap);
//        BallManagement.init(hardwareMap);
//        BallShooter.init(hardwareMap);
//        ButtonPush.init(hardwareMap);
//        BallLift.init(hardwareMap);
//        BallManagement.Intake(false, false);
//        BallManagement.Lift(false, false);
//        ButtonPush.FrontPushIn();
//        ButtonPush.RearPushIn();
//
//        // Chassis Sensor Init
//        csChasis = hardwareMap.colorSensor.get("csChasis");
//        //testColorSensor = (LightningColorSensor)hardwareMap.colorSensor.get("csChasis");
//
//        // Set up the parameters with which we will use our IMU. Note that integration
//        // algorithm here just reports accelerations to the logcat log; it doesn't actually
//        // provide positional information.
//        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
//        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
//        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
//        parameters.calibrationDataFile = "AdafruitIMUCalibration.json"; // see the calibration sample opmode
//        parameters.loggingEnabled      = true;
//        parameters.loggingTag          = "IMU";
//        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
//
//        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
//        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
//        // and named "imu".
//        imu = hardwareMap.get(BNO055IMU.class, "imu");
//        imu.initialize(parameters);
//
//        // Start the logging of measured acceleration
//        imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);
//
//        robot.leftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
//        robot.leftMotor2.setDirection(DcMotorSimple.Direction.REVERSE);
//
//        // Ensure the Motors it stationary, then reset the encoders and calibrate the gyro.
//        robot.leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//        robot.rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//
//        // Send telemetry message to alert driver that we are calibrating;
//        telemetry.addData(">", "Calibrating Gyro");    //
//        telemetry.update();
//
//        //gyro.calibrate();
//
//        // make sure the gyro is calibrated before continuing
//        //while (gyro.isCalibrating())  {
//        while (!imu.isGyroCalibrated())  {
//            Thread.sleep(50);
//            idle();
//        }
//
//        telemetry.addData(">", "Robot Ready.");    //
//        telemetry.update();
//
//        robot.leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        robot.rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//
//        // Wait for the game to start (Display Gyro value), and reset gyro before we move..
//        while (!isStarted()) {
//            angles   = imu.getAngularOrientation().toAxesReference(AxesReference.INTRINSIC).toAxesOrder(AxesOrder.ZYX);
//            telemetry.addData(">", "Robot Heading = %.1f", angleDegrees(angles.angleUnit, angles.firstAngle));
//            telemetry.update();
//            idle();
//        }
//        //gyro.resetZAxisIntegrator();
//        resetIMU_Position_Integration();
//
//        BallLift.armDrive1(-1);
//        Thread.sleep(500);
//        BallLift.armDrive1(0);
//
//        // Step through each leg of the path,
//        // Note: Reverse movement is obtained by setting a negative distance (not speed)
//        // Put a hold after each turn
//
//        //For Red Team Drive 8 inches more, then turn left
//        gyroDrive(DRIVE_SPEED, 44+8, 0.0);  // Drive FWD
//        Thread.sleep(750);
//
//        BallShooter.ShootBall();
//        Thread.sleep(500);
//
//        ElapsedTime holdTimer = new ElapsedTime();
//
//        // keep looping while we have time remaining.
//        holdTimer.reset();
//        while (opModeIsActive() && (holdTimer.time() < 4)) {  //2 seconds
//            BallManagement.Lift(true, false);
//            idle();
//        }
//
//        BallManagement.Lift(false, false);
//        BallShooter.StopShooter();
//
//        gyroDrive(DRIVE_SPEED, 44+4, 0.0);  // Drive FWD
//        Thread.sleep(750);
//
//        gyroTurn(TURN_SPEED, -45.0);        // Turn CW to 45 Degrees
//        Thread.sleep(750);
//
//        robot.leftMotor.setPower(.375);
//        robot.rightMotor.setPower(.375);
//        robot.leftMotor2.setPower(.375);
//        robot.rightMotor2.setPower(.375);
//        Thread.sleep(1500);
//        robot.leftMotor.setPower(0);
//        robot.rightMotor.setPower(0);
//        robot.leftMotor2.setPower(0);
//        robot.rightMotor2.setPower(0);
//
//        gyroDrive(DRIVE_SPEED, -3, 0.0);  // Drive FWD
//        Thread.sleep(750);
//        gyroTurn(TURN_SPEED, 120.0);        // Turn CW to 45 Degrees
//
//        ButtonPush.FrontPushOut();
//
//        //Try stopping the gyro to avoid any conflicts
//        //on the i2c bus with the color sensors
//        imu.stopAccelerationIntegration();
//        while (ButtonPush.csPushR.red() > 200)
//        {
//            idle();
//        }
//        Thread.sleep(1000);
//
//        robot.leftMotor.setPower(.25);
//        robot.rightMotor.setPower(.25);
//        robot.leftMotor2.setPower(.25);
//        robot.rightMotor2.setPower(.25);
//
////Keep just in case we get floor sensor to work
////        telemetry.addData("FColor", "%3d:%3d:%3d", csChasis.red(), csChasis.green() , csChasis.blue());
////        while(((csChasis.blue() + csChasis.red() + csChasis.green())/3) < 38)
////        {
////            telemetry.addData("FColor", "%3d:%3d:%3d", csChasis.red(), csChasis.green() , csChasis.blue());
////            telemetry.addData("BColor", "%3d:%3d:%3d", ButtonPush.csPushR.red(), ButtonPush.csPushR.green() , ButtonPush.csPushR.blue());
////            telemetry.update();
////            //Thread.sleep(2);
////            idle();
////        }
//
//        //Red Team
//        telemetry.addData("BColor", "%3d:%3d", ButtonPush.csPushR.red(), ButtonPush.csPushR.blue());
//        while(ButtonPush.csPushR.red() < 10)
//        {
//            //telemetry.addData("FColor", "%3d:%3d", csChasis.red(), csChasis.green() , csChasis.blue());
//            telemetry.addData("BColor", "%3d:%3d", ButtonPush.csPushR.red(), ButtonPush.csPushR.blue());
//            telemetry.update();
//            //Thread.sleep(2);
//            idle();
//        }
//        robot.leftMotor.setPower(0);
//        robot.rightMotor.setPower(0);
//        robot.leftMotor2.setPower(0);
//        robot.rightMotor2.setPower(0);
//
////        //Red Team
//////        telemetry.addData("BColor", "%3d:%3d:%3d", ButtonPush.csPushR.red(), ButtonPush.csPushR.green() + ButtonPush.csPushR.blue());
//////        telemetry.update();
////        if (ButtonPush.PollRearSensor() == Red) {
//            ButtonPush.RearPushOut();
////        }
////        else
////        {
////            ButtonPush.FrontPushOut();
////        }
//        Thread.sleep(1500);
//        ButtonPush.RearPushIn();
//        ButtonPush.FrontPushIn();
//        Thread.sleep(500);
//
////        gyroHold(DRIVE_SPEED, 45, 3);
////        gyroDrive(DRIVE_SPEED, 6, 0);  // Drive FWD 6 inches
//        //gyroHold(TURN_SPEED, -45.0, 0.5);   // Hold 45 Deg heading for 1/2 second
//
////        gyroDrive(DRIVE_SPEED, 48.0, 0.0);    // Drive FWD 48 inches
////        gyroTurn( TURN_SPEED, -45.0);         // Turn  CCW to -45 Degrees
////        gyroHold( TURN_SPEED, -45.0, 0.5);    // Hold -45 Deg heading for a 1/2 second
////        gyroTurn( TURN_SPEED,  45.0);         // Turn  CW  to  45 Degrees
////        gyroHold( TURN_SPEED,  45.0, 0.5);    // Hold  45 Deg heading for a 1/2 second
////        gyroTurn( TURN_SPEED,   0.0);         // Turn  CW  to   0 Degrees
////        gyroHold( TURN_SPEED,   0.0, 1.0);    // Hold  0 Deg heading for a 1 second
////        gyroDrive(DRIVE_SPEED,-48.0, 0.0);    // Drive REV 48 inches
////        gyroHold( TURN_SPEED,   0.0, 0.5);    // Hold  0 Deg heading for a 1/2 second
//
//        telemetry.addData("Path", "Complete");
//        telemetry.update();
//    }
//
//
//    /**
//     *  Method to drive on a fixed compass bearing (angle), based on encoder counts.
//     *  Move will stop if either of these conditions occur:
//     *  1) Move gets to the desired position
//     *  2) Driver stops the opmode running.
//     *
//     * @param speed      Target speed for forward motion.  Should allow for _/- variance for adjusting heading
//     * @param distance   Distance (in inches) to move from current position.  Negative distance means move backwards.
//     * @param angle      Absolute Angle (in Degrees) relative to last gyro reset.
//     *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
//     *                   If a relative angle is required, add/subtract from current heading.
//     */
//    public void gyroDrive ( double speed,
//                            double distance,
//                            double angle) throws InterruptedException {
//
//        int     newLeftTarget;
//        int     newRightTarget;
//        int     moveCounts;
//        double  max;
//        double  error;
//        double  steer;
//        double  leftSpeed;
//        double  rightSpeed;
//
//        // Ensure that the opmode is still active
//        if (opModeIsActive()) {
//
//            // Determine new target position, and pass to motor controller
//            moveCounts = (int)(distance * COUNTS_PER_INCH);
//            newLeftTarget = robot.leftMotor.getCurrentPosition() + moveCounts;
//            newRightTarget = robot.rightMotor.getCurrentPosition() + moveCounts;
//
//            // Set Target and Turn On RUN_TO_POSITION
//            robot.leftMotor.setTargetPosition(newLeftTarget);
//            robot.rightMotor.setTargetPosition(newRightTarget);
//
//            robot.leftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//            robot.rightMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//
//            // start motion.
//            speed = Range.clip(Math.abs(speed), 0.0, 1.0);
//            robot.leftMotor.setPower(speed);
//            robot.rightMotor.setPower(speed);
//            robot.leftMotor2.setPower(speed);
//            robot.rightMotor2.setPower(speed);
//
//            // keep looping while we are still active, and BOTH Motors are running.
//            while (opModeIsActive() &&
//                    (robot.leftMotor.isBusy() && robot.rightMotor.isBusy())) {
//
//                // adjust relative speed based on heading error.
//                error = getError(angle);
//                steer = getSteer(error, P_DRIVE_COEFF);
//
//                // if driving in reverse, the motor correction also needs to be reversed
//                if (distance < 0)
//                    steer *= -1.0;
//
//                leftSpeed = speed - steer;
//                rightSpeed = speed + steer;
//
//                // Normalize speeds if any one exceeds +/- 1.0;
//                max = Math.max(Math.abs(leftSpeed), Math.abs(rightSpeed));
//                if (max > 1.0)
//                {
//                    leftSpeed /= max;
//                    rightSpeed /= max;
//                }
//
//                robot.leftMotor.setPower(leftSpeed);
//                robot.rightMotor.setPower(rightSpeed);
//                robot.leftMotor2.setPower(leftSpeed);
//                robot.rightMotor2.setPower(rightSpeed);
//
//                if (distance > 0)
//                {
//                    if (robot.leftMotor.getCurrentPosition() >= newLeftTarget)
//                    {
//                        // Stop all motion;
//                        robot.leftMotor.setPower(0);
//                        robot.rightMotor.setPower(0);
//                        robot.leftMotor2.setPower(0);
//                        robot.rightMotor2.setPower(0);
//
//                        // Turn off RUN_TO_POSITION
//                        robot.leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//                        robot.rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//                        break;
//                    }
//                }
//
//                // Display drive status for the driver.
//                telemetry.addData("Err/St",  "%5.1f/%5.1f",  error, steer);
//                telemetry.addData("Target",  "%7d:%7d",      newLeftTarget,  newRightTarget);
//                telemetry.addData("ActualL",  "%7d:%7d",      robot.leftMotor.getCurrentPosition(),
//                        robot.rightMotor.getCurrentPosition());
//                telemetry.addData("Speed",   "%5.2f:%5.2f",  leftSpeed, rightSpeed);
////                telemetry.addData("FColor", "%3d:%3d:%3d", csChasis.red(), csChasis.green() + csChasis.blue());
////                telemetry.addData("BColor", "%3d:%3d:%3d", ButtonPush.csPushR.red(), ButtonPush.csPushR.green() + ButtonPush.csPushR.blue());
//                telemetry.update();
//
//                // Allow time for other processes to run.
//                idle();
//            }
//
//            // Stop all motion;
//            robot.leftMotor.setPower(0);
//            robot.rightMotor.setPower(0);
//            robot.leftMotor2.setPower(0);
//            robot.rightMotor2.setPower(0);
//
//            // Turn off RUN_TO_POSITION
//            robot.leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//            robot.rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        }
//    }
//
//    /**
//     *  Method to spin on central axis to point in a new direction.
//     *  Move will stop if either of these conditions occur:
//     *  1) Move gets to the heading (angle)
//     *  2) Driver stops the opmode running.
//     *
//     * @param speed Desired speed of turn.
//     * @param angle      Absolute Angle (in Degrees) relative to last gyro reset.
//     *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
//     *                   If a relative angle is required, add/subtract from current heading.
//     * @throws InterruptedException
//     */
//    public void gyroTurn (  double speed, double angle)
//            throws InterruptedException {
//
//        // keep looping while we are still active, and not on heading.
//        while (opModeIsActive() && !onHeading(speed, angle, P_TURN_COEFF)) {
//            // Update telemetry & Allow time for other processes to run.
//            telemetry.update();
//            idle();
//        }
//    }
//
//    /**
//     *  Method to obtain & hold a heading for a finite amount of time
//     *  Move will stop once the requested time has elapsed
//     *
//     * @param speed      Desired speed of turn.
//     * @param angle      Absolute Angle (in Degrees) relative to last gyro reset.
//     *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
//     *                   If a relative angle is required, add/subtract from current heading.
//     * @param holdTime   Length of time (in seconds) to hold the specified heading.
//     * @throws InterruptedException
//     */
//    public void gyroHold( double speed, double angle, double holdTime)
//            throws InterruptedException {
//
//        ElapsedTime holdTimer = new ElapsedTime();
//
//        // keep looping while we have time remaining.
//        holdTimer.reset();
//        while (opModeIsActive() && (holdTimer.time() < holdTime)) {
//            // Update telemetry & Allow time for other processes to run.
//            onHeading(speed, angle, P_TURN_COEFF);
//            telemetry.update();
//            idle();
//        }
//
//        // Stop all motion;
//        robot.leftMotor.setPower(0);
//        robot.rightMotor.setPower(0);
//        robot.leftMotor2.setPower(0);
//        robot.rightMotor2.setPower(0);
//    }
//
//    /**
//     * Perform one cycle of closed loop heading control.
//     *
//     * @param speed     Desired speed of turn.
//     * @param angle     Absolute Angle (in Degrees) relative to last gyro reset.
//     *                  0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
//     *                  If a relative angle is required, add/subtract from current heading.
//     * @param PCoeff    Proportional Gain coefficient
//     * @return
//     */
//    boolean onHeading(double speed, double angle, double PCoeff) {
//        double   error ;
//        double   steer ;
//        boolean  onTarget = false ;
//        double leftSpeed;
//        double rightSpeed;
//
//        // determine turn power based on +/- error
//        error = getError(angle);
//
//        if (Math.abs(error) <= HEADING_THRESHOLD) {
//            steer = 0.0;
//            leftSpeed  = 0.0;
//            rightSpeed = 0.0;
//            onTarget = true;
//        }
//        else {
//            steer = getSteer(error, PCoeff);
//            rightSpeed  = speed * steer;
//            leftSpeed   = -rightSpeed;
//        }
//
//        // Send desired speeds to Motors.
//        robot.leftMotor.setPower(leftSpeed);
//        robot.leftMotor2.setPower(leftSpeed);
//        robot.rightMotor.setPower(rightSpeed);
//        robot.rightMotor2.setPower(rightSpeed);
//
//        // Display it for the driver.
//        telemetry.addData("Target", "%5.2f", angle);
//        telemetry.addData("Err/St", "%5.2f/%5.2f", error, steer);
//        telemetry.addData("Speed.", "%5.2f:%5.2f", leftSpeed, rightSpeed);
//
//        return onTarget;
//    }
//
//    /**
//     * getError determines the error between the target angle and the Motors's current heading
//     * @param   targetAngle  Desired angle (relative to global reference established at last Gyro Reset).
//     * @return  error angle: Degrees in the range +/- 180. Centered on the Motors's frame of reference
//     *          +ve error means the Motors should turn LEFT (CCW) to reduce error.
//     */
//    public double getError(double targetAngle) {
//
//        double robotError;
//
//        // calculate error in -179 to +180 range  (
//        newAngles  = imu.getAngularOrientation().toAxesReference(AxesReference.INTRINSIC).toAxesOrder(AxesOrder.ZYX);
//        //robotError = targetAngle - gyro.getIntegratedZValue();
//        //robotError = targetAngle - AngleUnit.DEGREES.fromUnit(newAngles.angleUnit, newAngles.firstAngle);
//        robotError = targetAngle + AngleUnit.DEGREES.fromUnit(newAngles.angleUnit, newAngles.firstAngle);
//        while (robotError > 180)  robotError -= 360;
//        while (robotError <= -180) robotError += 360;
//        return robotError;
//    }
//
//    /**
//     * returns desired steering force.  +/- 1 range.  +ve = steer left
//     * @param error   Error angle in Motors relative degrees
//     * @param PCoeff  Proportional Gain Coefficient
//     * @return
//     */
//    public double getSteer(double error, double PCoeff) {
//        return Range.clip(error * PCoeff, -1, 1);
//    }
//
//    //----------------------------------------------------------------------------------------------
//    // Formatting
//    //----------------------------------------------------------------------------------------------
//
//    public double angleDegrees(AngleUnit angleUnit, double angle) {
//        return AngleUnit.DEGREES.fromUnit(angleUnit, angle);
//    }
//
//    String formatAngle(AngleUnit angleUnit, double angle) {
//        return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
//    }
//
//    String formatDegrees(double degrees){
//        return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
//        //return String.format("%.1f", AngleUnit.DEGREES.normalize(degrees));
//    }
//
//}
