/******************************************************
 **
 **  The Logical Clock
 **
 *******************************************************/
public class LogicalClock {

    int[] clockData;

    public LogicalClock(int ID)
    {
        clockData = new int[4];
        clockData[0]=ID;    //who's clock it is
        clockData[1]=0;     //current clock value
        clockData[2]=0;     //most recently received from ID
        clockData[3]=0;     //most recently received clock value
    }

    /*each process MUST do this before it does an event*/
    public void tickTock()
    {
        //System.out.println("you don't stop");
        clockData[1] += 1;      //event counter++
    }

    public void synchronize(LogicalClock incoming)
    {
        // update last received data
        this.clockData[2]=incoming.clockData[0];
        this.clockData[3]=incoming.clockData[1];

        // higher than current?
        if (incoming.clockData[1]>this.clockData[1]){
            this.clockData[1]=incoming.clockData[1];
        }

        // receiving/synchronizing IS an event so increment
        this.clockData[1] += 1;
    }

    public String getTimeStamp()
    {
        String time = "     client: ["+String.valueOf(clockData[0]+"]\n");
               time+="     current: ["+String.valueOf(clockData[1])+"]\n";
               time+=" received ID: ["+String.valueOf(clockData[2])+"]\n";
               time+="received Val: ["+String.valueOf(clockData[3])+"]\n";
        return time;
    }
}
