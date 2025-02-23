import java.io.*;
import java.util.*;

public class tes {
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

            int N = Integer.parseInt(baris1[0]);
            int M = Integer.parseInt(baris1[1]);
            int P = Integer.parseInt(baris1[2]);

            char[][] board = new char[N][M];
            for (int i = 0; i < N; i++) {
                Arrays.fill(board[i], '.'); // Isi semua sel dengan spasi (kosong)
            }

            String S = input.readLine();
            if (S == null) {
                System.out.println("Error: Tidak ada string S.");
                return;
            }
            S = S.trim();
            System.out.println();

            if (!S.equals("DEFAULT")){
                System.out.println("Mohon maaf, tidak tersedia jenis kasus " + S + ".");
                return;
            }

            List<char[][]> allpuzzles = new ArrayList<>();
            List<String> currentPuzzle = new ArrayList<>();
            
            Character lastFirstChar = null; // Huruf pertama dari puzzle sebelumnya (bukan spasi)

            while (input.ready()) {
                String line = input.readLine();
                if (line.trim().isEmpty()) continue; // Lewati baris kosong

                // Cari huruf pertama (bukan spasi)
                int firstCharIndex = findFirstCharIndex(line);
                if (firstCharIndex == -1) continue; // Kalau barisnya cuma spasi, lewati

                char firstChar = line.charAt(firstCharIndex);

                // Kalau ini puzzle pertama, ambil huruf pertamanya
                if (lastFirstChar == null) {
                    lastFirstChar = firstChar;
                }

                // Bandingkan huruf pertama dengan puzzle sebelumnya
                if (firstChar == lastFirstChar) {
                    currentPuzzle.add(line); // Masukkan ke puzzle sebelumnya
                } else {
                    // Simpan puzzle lama sebelum mulai yang baru
                    if (!currentPuzzle.isEmpty()) {
                        allpuzzles.add(convertToCharArray(currentPuzzle));
                        currentPuzzle.clear();
                    }
                    currentPuzzle.add(line);
                    lastFirstChar = firstChar; // Update huruf pertama puzzle baru
                }
            }

            // Tambahkan puzzle terakhir jika ada
            if (!currentPuzzle.isEmpty()) {
                allpuzzles.add(convertToCharArray(currentPuzzle));
            }
            
            input.close();

            if (allpuzzles.size() != P){
                System.out.println("Banyaknya puzzle tidak sama dengan nilai P. Silakan coba lagi.");
                return;
            }
        
            printPuzzles(allpuzzles, colorMap, outputtxt);
        
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

                try (BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile))) {
                    outputWriter.write(outputtxt.toString());
                    System.out.println("Output berhasil disimpan ke: " + outputFile);
                    System.out.println();
                } catch (IOException e) {
                    System.out.println("Error menyimpan file: " + e.getMessage());
                }
            }
            scanner.close();
            System.out.println("Game telah selesai. Terima kasih telah bermain.");
        }
    }

    private static int findFirstCharIndex(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != ' ') return i;
        }
        return -1; // Semua spasi
    }

    private static char[][] convertToCharArray(List<String> puzzleStrings) {
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

    // debug
    private static void printPuzzles(List<char[][]> allpuzzles, Map<Character, String> colorMap, StringBuilder outputtxt) {
        System.out.println("\n=== Puzzles ===");
        for (int i = 0; i < allpuzzles.size(); i++) {
            //System.out.println("Puzzle " + (i + 1) + ":");
            for (char[] row : allpuzzles.get(i)) {
                // Cek isi row sebelum ditampilkan
                //System.out.println(Arrays.toString(row)); // Debug: Print array as is
                for (char c : row) {
                    String color = colorMap.get(c); // Ambil warna dari colorMap
                    System.out.print(color + c + "\u001B[0m"); // Warna + Huruf + Reset
                    outputtxt.append(color + c + "\u001B[0m").append("\n");
                }                
                System.out.println();
                outputtxt.append("\n");
            }
            //System.out.println();
        }
        System.out.println("=== Debug: Semua Puzzle ===");
        for (char[][] p : allpuzzles) {
            for (char[] row : p) {
                System.out.println(Arrays.toString(row));
                outputtxt.append(Arrays.toString(row)).append("\n");
            }
            System.out.println();
            outputtxt.append("\n");
        }

    }    
    private static boolean solve(int index, List<char[][]> allpuzzles, char[][] board, int N, int M, Map<Character, String> colorMap, StringBuilder outputtxt) {
        //System.out.println("Mencoba puzzle ke-" + index);
        //printBoard(board);

        if (index == allpuzzles.size()) {
            if (hasEmptySpace(board,N,M)){
                return false;
            }

            printBoard(board, colorMap, outputtxt);
            return true; // Semua puzzle sudah ditempatkan
        }
    
        char[][] puzzle = allpuzzles.get(index);
    
        // Coba semua posisi di papan
        for (int i=0;i<N;i++) {
            for (int j=0;j<M;j++) {
                for (int k=0;k<4;k++) { // coba semua rotasi
                    char[][] rotated = rotate(puzzle,k);
                    char[][] flippedH = flipHorizontal(rotated);
                    char[][] flippedV = flipVertical(rotated);
                    if (canPlace(rotated, board, i, j, N, M)) {
                        placePuzzle(rotated, board, i, j);
                        if (tryPlacement(rotated, board, i, j, N, M, index, allpuzzles, colorMap, outputtxt)) return true;
                        removePuzzle(rotated, board, i, j); // Backtrack
                    }
                    
                    if (canPlace(flippedH, board, i, j, N, M)) {
                        placePuzzle(flippedH, board, i, j);
                        if (tryPlacement(flippedH, board, i, j, N, M, index, allpuzzles, colorMap, outputtxt)) return true;
                        removePuzzle(flippedH, board, i, j); // Backtrack
                    }
                    
                    if (canPlace(flippedV, board, i, j, N, M)) {
                        placePuzzle(flippedV, board, i, j);
                        if (tryPlacement(flippedV, board, i, j, N, M, index, allpuzzles, colorMap, outputtxt)) return true;
                        removePuzzle(flippedV, board, i, j); // Backtrack
                    }                    
                }
            }
        }
        return false; // Tidak ada solusi dari posisi ini
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

    private static boolean tryPlacement(char[][] puzzle, char[][] board, int i, int j, int N, int M, int index, List<char[][]> allpuzzles, Map<Character, String> colorMap, StringBuilder outputtxt){
        if (solve(index + 1, allpuzzles, board, N, M, colorMap, outputtxt)) {
            return true;
        }
        removePuzzle(puzzle, board, i, j); // Backtrack kalau gagal
        return false;
    }


    private static boolean canPlace(char[][] puzzle, char[][] board, int x, int y, int N, int M){
        totalAttempts++;
        for (int i=0;i<puzzle.length;i++){
            for (int j=0;j<puzzle[i].length;j++){
                if (puzzle[i][j] != ' ' && (x+i >= N || y+j >= M || board[x+i][y+j] != '.')){
                    //System.out.println("Tidak bisa menempatkan di (" + x + "," + y + ")");
                    return false; 
                }
            }
        }
        //System.out.println("Bisa menempatkan di (" + x + "," + y + ")");
        return true;
    }

    private static void placePuzzle (char[][] puzzle, char[][] board, int x, int y){
        //System.out.println("Mencoba tempatkan puzzle di (" + x + "," + y + ")");

        for (int i=0;i<puzzle.length;i++){
            for (int j=0;j<puzzle[i].length;j++){
                if (puzzle[i][j] != ' '){
                    board[x+i][y+j] = puzzle[i][j];
                }
            }
        }
    }

    private static void removePuzzle (char[][] puzzle, char[][] board, int row, int col){
        //System.out.println("Menghapus puzzle dari (" + row + "," + col + ")");

        for (int i=0;i<puzzle.length;i++){
            for (int j=0;j<puzzle[i].length;j++){
                if (puzzle[i][j] != ' '){
                    board[row+i][col+j] = '.';
                }
            }
        }
    }

    private static char[][] rotate(char[][] puzzle, int putar){
        // if (puzzle.length == 0 || puzzle[0].length == 0) {
        //     return puzzle; // Hindari error dengan langsung mengembalikan puzzle kosong
        // }
        //System.out.println("Rotating puzzle: " + puzzle.length + "x" + (puzzle.length > 0 ? puzzle[0].length : 0));
        
        char[][] rotated = puzzle;
        for (int i=0;i<putar;i++){

            int row = rotated.length;
            //if (row == 0) return puzzle;

            int col = rotated[0].length;
            //if (col == 0) return puzzle;

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

    private static char[][] flipVertical(char[][] puzzle){
        int row = puzzle.length, col = puzzle[0].length;
        char[][] flipped = new char[row][col];
        for (int i=0;i<row;i++){
            for (int j=0;j<col;j++){
                flipped[i][col-j-1] = puzzle[i][j];
            }
        }
        return flipped;
    }

    private static void printBoard (char[][] board, Map<Character, String> colorMap, StringBuilder outputtxt){
        String solusi = "\n=== Solusi Akhir ===";
        System.out.println(solusi);
        outputtxt.append(solusi).append("\n");

        for (char[] row : board){
            System.out.println(Arrays.toString(row));
            outputtxt.append(Arrays.toString(row)).append("\n");
        }
        System.out.println();
        for (char[] row : board){
            StringBuilder line = new StringBuilder();
            for (char c : row){
                String color = colorMap.get(c);
                line.append(color).append(c).append("\u001B[0m");
            }
            System.out.println(line.toString());
            outputtxt.append(line.toString()).append("\n");
        }
        System.out.println();
        outputtxt.append("\n");
    }
}