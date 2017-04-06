/**
 * Created by sophiegao on 3/27/17.
 */
import java.io.*;

public class SeatAssigning {
    public static void main(String[] args) {
        // Create reservedSeat array
        String reservedSeat[] = new String[] {"R1C4","R1C6","R2C3","R2C7","R3C9","R3C10"};
        SeatAssigning sistersShow = build(3, 11, reservedSeat);
        SeatAssigning tempChart;

        sistersShow.displayMap();

        /* ---------------------------- User Input Part------------------------------ */
        // create "run" as a judge sign to proceed or stops the program execution
        boolean run = true;
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        // userInput represents users' input from the command line
        String userInput;
        // seatsNeeded represents the number of seats that users' requests to be reserved;
        int seatsNeeded;
        while (run) {
            System.out.println("Welcome my dear guest, Would you like to reserve some seats? ");
            try {
                userInput = console.readLine();
            }
            catch (IOException err) {
                break;
            }
            // set reserve operation input by answer No or Yes
            if (userInput.equalsIgnoreCase("No")) {
                run = false;
                continue;
            }
            else if (userInput.equalsIgnoreCase("Yes")) {
            }
            else {
                // remind users if the input can't be proceeded
                System.out.println("Please input yes or no answers only. Thanks! ");
                continue;
            }
            System.out.println("How many seats do you need to reserve, please? ");
            try {
                userInput = console.readLine();
            }
            catch (IOException err) {
                break;
            }
            try {
                tempChart = reserve(sistersShow, Integer.parseInt(userInput));
            }
            catch (NumberFormatException err){
                System.out.println(" It's not a valid number!");
                continue;
            }

            if (!tempChart.equals(sistersShow)) {
                tempChart.displayMap();
                System.out.println("Are you sure to save these seats, please?");

                try {
                    userInput = console.readLine();
                }
                catch (IOException err) {
                    // You don't have a keyboard? Really? Just screw it, I'm going home then
                    break;
                }
                if (userInput.equalsIgnoreCase("Yes")) {
                    sistersShow = new SeatAssigning(tempChart);
                }
            }
        }
    }

    /* ---------------------------- Seat Assigning Part------------------------------ */
    // Implement seating assigning method: build and reserve
    private static SeatAssigning build(int rows, int cols, String reserved[]) {
        return new SeatAssigning(rows, cols, reserved);
    }
    private static SeatAssigning reserve(SeatAssigning map, int n) {
        // select available seats
        SeatAssigning tmpMap = new SeatAssigning(map);
        // Size check
        if (map.isTooSmall(n) || n > 10) {
            System.out.println("Not available.");
            return tmpMap;
        }
        int iRow = -1, iCol = -1;	// Start of the found block
        boolean chart[][] = tmpMap.getChart();
        // check row number
        int rows = chart.length;
        // check column number
        int cols = chart[0].length;
        int startC = 0;
        // Best seat
        int mid = tmpMap.getBestSeat();
        int iCompZone[] = new int[2];	// Indexes for the complex (overlap) zone of a row
        iCompZone[0] = mid - n + 2;		// Start
        iCompZone[1] = mid - 1;			// End
        mid = mid - n/2;				// Start of the best block

        for (int r = 0; r < rows; r++) {
            int start = -1;
            int count = 0;
            int tempR = -1, tempC = -1;
            boolean foundBest = false;	// Flag, once we've found the best block in a row
            for (int c = startC; c < cols && !foundBest; c++) {

                if (count == n && start > mid && tempR < 0) {
                    // Stop looking cause there won't be a better one
                    tempR = r;
                    tempC = start;
                    break;
                }

                if (!chart[r][c]) {
                    count++;
                    if (start < 0) {
                        start = c;
                    }
                }

                else if (count < n) {
                    count = 0;
                    start = -1;
                }

                if ((chart[r][c] || c == cols - 1) && count >= n) {
                    // Find closest block to the middle for the nontrivia cases
                    if (count != n) {
                        // NOTE: On exit, start should be the start of the best block
                        int lastPoss = start + count - n;	// Last index that a block could start at

                        // First the trivias
                        if (lastPoss < mid) {
                            // All starting points to the left of the best start
                            start = lastPoss;
                        }
                        else if (start == mid) {
                            // Start is set right and we have the best
                            foundBest = true;
                        }
                        else if (lastPoss == mid) {
                            start = lastPoss;
                            // We have the best
                            foundBest = true;
                        }
                        else if (start < mid && lastPoss > mid) {
                            // Mid is in range
                            start = mid;
                            // We have the best
                            foundBest = true;
                        }
                        else {
                            // Start is the start, also, best block of the row
                            foundBest = true;
                        }
                    }

                    if (tempR < 0) {
                        // Haven't found one yet
                        tempR = r;
                        tempC = start;
                        start = -1;
                        count = 0;
                    }
                    else {
                        // We already have one
                        if (Math.abs(tempC - mid) > Math.abs(start - mid)) {
                            // The one we have is better, this also means we have the best one
                            break;
                        }

                        else if (Math.abs(tempC - mid) == Math.abs(start - mid)){
                            // Duplicates? Randomly choose one
                            tempC = Math.random() < 0.5d ? tempC : start;
                            // Again, we're done here with this loop
                            break;
                        }

                        else {
                            // New block is better
                            tempC = start;
                            start = -1;
                            count = 0;
                        }
                    }
                }
            }

            if (tempR >= 0) {
                boolean updateChecks = false;	// Flag, flipped if we found a new/better block
                if (iRow < 0) {
                    // First block found
                    iRow = tempR;
                    iCol = tempC;
                    updateChecks = true;
                }

                else {
                    // Nontrivia
                    if (((iCol < iCompZone[0] || iCol > iCompZone[1]) &&
                            (tempC < iCompZone[0] || tempC > iCompZone[1])) ||
                            ((iCol > iCompZone[0] && iCol < iCompZone[1]) &&
                                    (tempC > iCompZone[0] && tempC < iCompZone[1]))) {
                        // So long as both are in or out of the zone, the math is easy
                        int diffR = tempR - iRow;	// Penalize the new block for being further back
                        if (Math.abs(iCol - mid) > (Math.abs(tempC - mid) + diffR)) {
                            // Old one is worse
                            iRow = tempR;
                            iCol = tempC;
                            updateChecks = true;
                        }
                    }
                    else {
                        // If only one is in the complicated zone, it's complicated
                        int realMid = tmpMap.getBestSeat();
                        double curAvg = 0.0, newAvg = 0.0;

                        for (int i = iCol; i < iCol + n; i++) {
                            curAvg += Math.abs(i - realMid) + iRow;
                        }
                        curAvg = curAvg/n;

                        for (int i = tempC; i < tempC + n; i++) {
                            newAvg += Math.abs(i - realMid) + tempR;
                        }
                        newAvg = newAvg/n;

                        if (curAvg < newAvg) {
                            iRow = tempR;
                            iCol = tempC;
                            updateChecks = true;
                        }
                    }
                }

                if (updateChecks) {
                    int blockMid = iCol + (n - 1)/2;
                    rows = Math.abs(tmpMap.getBestSeat() - blockMid) + iRow; // A close enough approximation
                    if (rows > chart.length) {
                        // Make sure we aren't looking at rows that aren't there
                        rows = chart.length;
                    }
                    // The seats must be closer to the middle than this one
                    startC = iCol;
                    cols = chart[0].length - (iCol + 1);
                }

                if (iRow >= 0) {
                    // The further back we go, the closer the seats have to be
                    startC++;
                    cols--;
                    if (startC > chart[0].length || cols < startC) {
                        // Shouldn't be here, but break if this happens
                        break;
                    }
                }
            }

        }

        // Print out seating reserving result
        if (iRow < 0) {
            System.out.println("No available seats could be reserved.");
        }
        else {
            for (int c = iCol; c < iCol + n; c++) {
                tmpMap.book(iRow, c);
            }
            System.out.printf("Your seats are reserved at R%dC%d through R%dC%d.\n",
                    iRow+1, iCol+1, iRow+1, (iCol+n));
        }

        return tmpMap;
    }

    /*----------------------- Definition about SeatAssigning class -------------------------*/
    // Fields
    private int rows;					// Number of rows the venue has
    private int columns;				// Number of columns (or seats per row) a venue has
    private boolean reservedSeats[][];	// The status of the seats, false if available
    private int middle;					// The index for the center seat of a row
    // Note: For middle, we want there to be more seats greater than middle than less than

    // Contructors

    public SeatAssigning(int r, int c) {
        // This constructor builds the class fully and sets all the seats to avaiable.
        // This is provided that the user didn't mess things up and give us bad values...

        rows = r;
        columns = c;
        if (rows < 1 || columns < 1) {
            // A zero or negative number is not valid input, go to error state
            middle = -1;
            reservedSeats = null;
        }
        else {
            middle = (c-1)/2;	// This gets the right index for both evens and odds
            reservedSeats = new boolean[r][c];	// In Java, this inializes to false
        }
    }
    // Build a constructor, reserves seats specified in res[]
    public SeatAssigning(int r, int c, String res[]) {
        // Will throw an error once that is added
        this(r, c);

        // Now for the reserving part!
        for (int i = 0; i < res.length; i++) {
            // The string format is "R#C#", with # being an unknown number of digits
            // avoid considering casing
            String code = res[i].toUpperCase();
            // reserve by Row and Column	
            int tRow = code.indexOf('R');
            int tCol = code.indexOf('C');

            // Also, we start those two temps off with the index of R and C so that...
            String tester = new String();
            try {
                // Remember to add 1 to the first index get skip the letter code
                tester = code.substring(tRow+1, tCol);
                tRow = Integer.parseInt(tester) - 1;
                tester = code.substring(tCol+1);
                tCol = Integer.parseInt(tester) - 1;
                // Oh and dipshit, most people aren't programmers! Row 1 = index 0 >:(
            }
            catch (NumberFormatException err) {
                // This is possible, just skip this string
                // Use this for simple error checking to standard output
                //System.out.printf("The String: %s at index: %d is not valid.\n", tester, i);
                continue;
            }
            //System.out.printf("row = %d; column = %d\n", tRow, tCol);
            reservedSeats[tRow][tCol] = true;
        }
    }

    //  ???
    // Copy constructor
    public SeatAssigning(SeatAssigning sc) {
        this(sc.rows, sc.columns);

        // Now to clone the array
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                reservedSeats[r][c] = sc.reservedSeats[r][c];
            }
        }
    }

    //Formated String representing the SeatAssigning class
    // Seats will be separated by a comma
    // Each row ends with a semicolon
    // starting with the first seat of the last row
    public String toString() {
        // A represents available seat
        String free = "A";
        // R represents reserved seat
        String reserved = "R";
        String seating = new String();

        for (int r = reservedSeats.length-1; r >= 0; r--) {
            // Starts with the last row going forward

            String temp = "";
            for (int c = 0; c < reservedSeats[r].length; c++) {
                // Starts with the left most seat
                if (c != 0) {
                    temp = temp + ",";
                }
                // If it's reserved...
                if (reservedSeats[r][c]) {
                    temp = temp + reserved;
                }
                // ...Otherwise
                else {
                    temp = temp + free;
                }
            }
            seating = temp + ";";
        }
        return seating;
    }

    public boolean book(int r, int c) {
        // Reserves a seat at r, c, returns false if seat is already booked
        if (!reservedSeats[r][c]){
            reservedSeats[r][c] = true;
            return true;
        }
        else {
            return false;
        }
    }

    public int getBestSeat() {
        // returns the index for the best seat in the house!
        return middle;
    }

    public void displayMap() {
        // display current seat map on a 3 x 11 chart
        for (int r = reservedSeats.length-1; r >= 0; r--) {
            System.out.printf("Row%3d: ", r+1);
            for (int c = 0; c < reservedSeats[r].length; c++) {
                if (c != 0) {
                    System.out.printf(",");
                }
                // if the seat is reserved, print R
                if (reservedSeats[r][c]) {
                    System.out.printf(" R");
                }
                // if the seat is available, print __
                else{
                    System.out.printf("__");
                }
            }
            System.out.printf("\n");
        }
        System.out.printf("      |");
        for (int i = 1; i <= reservedSeats[0].length; i++) {
            System.out.printf("%2d ", i);
        }
        System.out.printf("\n");
    }

    public boolean isTooSmall(int num) {
        return num > columns;
    }

    public boolean[][] getChart(){
        return reservedSeats;
    }

    public boolean equals(SeatAssigning sc) {
        // returns true if this SeatAssigning is equal to another
        if (rows != sc.rows || columns != sc.columns) {
            return false;
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (reservedSeats[r][c] ^ sc.reservedSeats[r][c]) {
                    return false;
                }
            }
        }

        return true;
    }
}
