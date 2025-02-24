import java.io.*;
import java.util.*;

public class IQPuzzlerPro {
    public static final String RESET = "\033[38;5;0m";
    public static final String[] ANSI_COLORS = {
        "\033[38;5;196m", "\033[38;5;226m", "\033[38;5;46m", 
        "\033[38;5;208m", "\033[38;5;30m", "\033[38;5;99m", 
        "\033[38;5;51m", "\033[38;5;18m", "\033[38;5;81m", 
        "\033[38;5;90m","\033[38;5;21m", "\033[38;5;201m",
        "\033[38;5;206m","\033[38;5;130m","\033[38;5;218m",
        "\033[38;5;88m", "\033[38;5;180m", "\033[38;5;100m", 
        "\033[38;5;220m", "\033[38;5;22m", "\033[38;5;87m", 
        "\033[38;5;147m", "\033[38;5;15m", "\033[38;5;89m", 
        "\033[38;5;240m", "\033[38;5;252m"  
    };
    private static int totalAttempts = 0;
    public static void main(String[] args) throws FileNotFoundException, IOException{
        Map<Character, String> colorMap = new HashMap<>();
        for (int i = 0; i < 26; i++) {
            colorMap.put((char) ('A' + i), ANSI_COLORS[i % ANSI_COLORS.length]); 
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Masukkan nama file txt: ");
        String file = scanner.nextLine();

        StringBuilder outputtxt = new StringBuilder();

        try (BufferedReader input = new BufferedReader(new FileReader(file))){
            String[] baris1 = input.readLine().split(" ");
            if (baris1.length < 3) {
                System.out.println("Error: Format file tidak sesuai.");
                return;
            }

            int N,M,P;
            try{
                N = Integer.parseInt(baris1[0]);
                M = Integer.parseInt(baris1[1]);
                P = Integer.parseInt(baris1[2]);
            } catch (NumberFormatException e){
                System.out.println("N, M, atau P bukan angka valid. Silakan coba lagi.");
                return;
            }

            char[][] board = new char[N][M];
            for (int i = 0; i < N; i++) {
                Arrays.fill(board[i], '.'); // Isi semua sel dengan spasi (kosong)
            }

            String S = input.readLine();
            S = S.trim();
            System.out.println();

            if (!S.equals("DEFAULT")){
                System.out.println("Mohon maaf, tidak tersedia jenis kasus " + S + ".");
                return;
            }

            List<char[][]> allpuzzles = new ArrayList<>();
            List<String> currentPuzzle = new ArrayList<>();
            
            Character lastFirstChar = null; 

            while (input.ready()) {
                String line = input.readLine();
                if (line.trim().isEmpty()) continue; 

                int firstCharIndex = findFirstChar(line);
                if (firstCharIndex == -1) continue;

                char firstChar = line.charAt(firstCharIndex);

                if (lastFirstChar == null) {
                    lastFirstChar = firstChar;
                }

                if (firstChar == lastFirstChar) {
                    currentPuzzle.add(line);
                } else {
                    if (!currentPuzzle.isEmpty()) {
                        allpuzzles.add(convertStringtoChar(currentPuzzle));
                        currentPuzzle.clear();
                    }
                    currentPuzzle.add(line);
                    lastFirstChar = firstChar; 
                }
            }

            if (!currentPuzzle.isEmpty()) {
                allpuzzles.add(convertStringtoChar(currentPuzzle));
            }
            
            input.close();

            if (allpuzzles.size() != P){
                System.out.println("Banyaknya puzzle tidak sama dengan nilai P. Silakan coba lagi.");
                return;
            }
        
            if (allpuzzles.isEmpty()) {
                System.out.println("Error: Tidak ada puzzle yang terbaca dari file.");
                return;
            }

            long startTime = System.nanoTime();

            System.out.println("Mencari solusi...");
            
            if (!solve(0, allpuzzles, board, N, M, colorMap, outputtxt)) {
                String nosolution = "Tidak ada solusi.";
                System.out.println(nosolution);
                outputtxt.append(nosolution).append("\n");
            }

            long endTime = System.nanoTime();
            long totalWaktu = endTime - startTime;
            String waktu = "Waktu pencarian: " + (totalWaktu / 1_000_000.0) + " ms";
            System.out.println(waktu);
            outputtxt.append(waktu).append("\n");

            System.out.println();
            outputtxt.append("\n");
            String totalKasus = "Banyak kasus yang ditinjau: " + totalAttempts;
            System.out.println(totalKasus);
            outputtxt.append(totalKasus).append("\n");

            System.out.println();
            System.out.println("Apakah anda ingin menyimpan solusi? (ya/tidak)");
            String simpan = scanner.nextLine().trim().toLowerCase();
            System.out.println();

            while (!simpan.equals("ya") && !simpan.equals("tidak")){
                System.out.println("Input tidak valid. Silakan masukan kembali.");
                System.out.println("Apakah anda ingin menyimpan solusi? (ya/tidak)");
                simpan = scanner.nextLine().trim().toLowerCase();
            }
            System.out.println();
            
            if (simpan.equals("ya")){
                System.out.print("Masukkan nama file output txt: ");
                String outputFile = scanner.nextLine();

                String outputDir = "../test"; 
                File outputFolder = new File(outputDir);

                if (!outputFolder.exists()) {
                    outputFolder.mkdir();
                }

                String outputFilePath = outputDir + "/" + outputFile;

                try (BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFilePath))) {
                    outputWriter.write(outputtxt.toString());
                    System.out.println("Output berhasil disimpan ke: " + outputFilePath);
                    System.out.println();
                } catch (IOException e) {
                    System.out.println("Error menyimpan file: " + e.getMessage());
                    System.out.println();
                }
            }
            scanner.close();
            System.out.println("IQPuzzlerPro telah selesai. Terima kasih telah bermain.");
        }
    }

    private static int findFirstChar(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != ' ') return i;
        }
        return -1;
    }

    private static char[][] convertStringtoChar(List<String> puzzleStrings) {
        int rows = puzzleStrings.size();
        int cols = puzzleStrings.stream().mapToInt(String::length).max().orElse(0);
        char[][] puzzle = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            String row = puzzleStrings.get(i);
            for (int j = 0; j < cols; j++) {
                puzzle[i][j] = (j < row.length()) ? row.charAt(j) : ' ';
            }
        }
        return puzzle;
    }

    private static boolean solve(int index, List<char[][]> allpuzzles, char[][] board, int N, int M, Map<Character, String> colorMap, StringBuilder outputtxt) {
        if (index == allpuzzles.size()) {
            if (hasEmptySpace(board,N,M)){
                return false;
            }

            printBoard(board, colorMap, outputtxt);
            return true; 
        }
    
        char[][] puzzle = allpuzzles.get(index);

        for (int i=0;i<N;i++) {
            for (int j=0;j<M;j++) {
                for (int k=0;k<4;k++) { 
                    char[][] rotated = rotate(puzzle,k);
                    char[][] flippedH = flipHorizontal(rotated);

                    if (canPlace(rotated, board, i, j, N, M)) {
                        placePuzzle(rotated, board, i, j);
                        if (nextPuzzle(rotated, board, i, j, N, M, index, allpuzzles, colorMap, outputtxt)) return true;
                        removePuzzle(rotated, board, i, j);
                    }
                    
                    if (canPlace(flippedH, board, i, j, N, M)) {
                        placePuzzle(flippedH, board, i, j);
                        if (nextPuzzle(flippedH, board, i, j, N, M, index, allpuzzles, colorMap, outputtxt)) return true;
                        removePuzzle(flippedH, board, i, j); 
                    }                  
                }
            }
        }
        return false; 
    }    

    private static boolean hasEmptySpace(char[][] board, int N, int M){
        for (int i=0;i<N;i++){
            for (int j=0;j<M;j++){
                if (board[i][j] == '.'){
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean nextPuzzle(char[][] puzzle, char[][] board, int i, int j, int N, int M, int index, List<char[][]> allpuzzles, Map<Character, String> colorMap, StringBuilder outputtxt){
        if (solve(index + 1, allpuzzles, board, N, M, colorMap, outputtxt)) {
            return true;
        }
        removePuzzle(puzzle, board, i, j); 
        return false;
    }

    private static boolean canPlace(char[][] puzzle, char[][] board, int x, int y, int N, int M){
        totalAttempts++;
        for (int i=0;i<puzzle.length;i++){
            for (int j=0;j<puzzle[i].length;j++){
                if (puzzle[i][j] != ' ' && (x+i >= N || y+j >= M || board[x+i][y+j] != '.')){
                    return false; 
                }
            }
        }
        return true;
    }

    private static void placePuzzle (char[][] puzzle, char[][] board, int x, int y){
        for (int i=0;i<puzzle.length;i++){
            for (int j=0;j<puzzle[i].length;j++){
                if (puzzle[i][j] != ' '){
                    board[x+i][y+j] = puzzle[i][j];
                }
            }
        }
    }

    private static void removePuzzle (char[][] puzzle, char[][] board, int row, int col){
        for (int i=0;i<puzzle.length;i++){
            for (int j=0;j<puzzle[i].length;j++){
                if (puzzle[i][j] != ' '){
                    board[row+i][col+j] = '.';
                }
            }
        }
    }

    private static char[][] rotate(char[][] puzzle, int putar){
        char[][] rotated = puzzle;
        for (int i=0;i<putar;i++){
            int row = rotated.length;
            int col = rotated[0].length;
            char[][] newRotated = new char[col][row];

            for (int j=0;j<row;j++){
                for (int k=0;k<col;k++){
                    newRotated[k][row-j-1] = rotated[j][k];
                }
            }
            rotated = newRotated;
        }
        return rotated;
    }

    private static char[][] flipHorizontal(char[][] puzzle){
        int row = puzzle.length;
        char[][] flipped = new char[row][];
        for (int i=0;i<row;i++){
            flipped[i] = puzzle[row-i-1];
        }
        return flipped;
    }

    private static void printBoard (char[][] board, Map<Character, String> colorMap, StringBuilder outputtxt){
        String printsolusi = "\n=== Solusi Akhir ===";
        System.out.println(printsolusi);
        System.out.println();
        outputtxt.append(printsolusi).append("\n");
        outputtxt.append("\n");

        for (int i = 0; i < board.length; i++){
            StringBuilder solusi = new StringBuilder();
            StringBuilder solusitxt = new StringBuilder();

            for (int j = 0; j < board[i].length; j++){
                char c = board[i][j];
                String color = colorMap.get(c);
                solusi.append(color).append(c).append("\u001B[0m");
                solusitxt.append(c);
            }
            System.out.println(solusi.toString());
            outputtxt.append(solusitxt.toString()).append("\n");
        }
        System.out.println();
        outputtxt.append("\n");
    }
}