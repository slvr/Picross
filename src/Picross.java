/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.*;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.toList;
 
public class Picross {
 
    static String[] p1 = {"I BB BBB BBD ABBA ABBA ABBA AKA AKA ABA ABA BBB CB BB I", 
    		"I BB BCB BDD ABBBA ABDA ABCA AABA ACA ADA ABBA BBBB CBB BB I"};
 
    public static void main(String[] args) {
        for (String[] puzzleData : new String[][]{p1})
            newPuzzle(puzzleData);
    }
 
    static void newPuzzle(String[] data) {
        String[] rowData = data[0].split("\\s");
        String[] colData = data[1].split("\\s");
 
        List<List<BitSet>> cols, rows;
        rows = getCandidates(rowData, colData.length);
        cols = getCandidates(colData, rowData.length);
 
        int numChanged;
        do {
            numChanged = reduceMutual(cols, rows);
            if (numChanged == -1) {
                System.out.println("No solution");
                return;
            }
        } while (numChanged > 0);
 
        rows.stream().map((row) -> {
            for (int i = 0; i < cols.size(); i++)
                System.out.print(row.get(0).get(i) ? "# " : ". ");
            return row;
        }).forEachOrdered((_item) -> {
            System.out.println();
        });
        System.out.println();
    }
 
    // collect all possible solutions for the given clues
    static List<List<BitSet>> getCandidates(String[] data, int len) {
        List<List<BitSet>> result = new ArrayList<>();
 
        for (String s : data) {
            List<BitSet> lst = new LinkedList<>();
 
            int sumChars = s.chars().map(c -> c - 'A' + 1).sum();
            List<String> prep = stream(s.split(""))
                    .map(x -> repeat(x.charAt(0) - 'A' + 1, "1")).collect(toList());
 
            genSequence(prep, len - sumChars + 1).stream().map((r) -> r.substring(1).toCharArray()).map((bits) -> {
                BitSet bitset = new BitSet(bits.length);
                for (int i = 0; i < bits.length; i++)
                    bitset.set(i, bits[i] == '1');
                return bitset;
            }).forEachOrdered((bitset) -> {
                lst.add(bitset);
            });
            result.add(lst);
        }
        return result;
    }
 
    // permutation generator, translated from Python via D
    static List<String> genSequence(List<String> ones, int numZeros) {
        if (ones.isEmpty())
            return asList(repeat(numZeros, "0"));
 
        List<String> result = new ArrayList<>();
        for (int x = 1; x < numZeros - ones.size() + 2; x++) {
            List<String> skipOne = ones.stream().skip(1).collect(toList());
            for (String tail : genSequence(skipOne, numZeros - x))
                result.add(repeat(x, "0") + ones.get(0) + tail);
        }
        return result;
    }
 
    static String repeat(int n, String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++)
            sb.append(s);
        return sb.toString();
    }
 
    /* If all the candidates for a row have a value in common for a certain cell,
    then it's the only possible outcome, and all the candidates from the
    corresponding column need to have that value for that cell too. The ones
    that don't, are removed. The same for all columns. It goes back and forth,
    until no more candidates can be removed or a list is empty (failure). */
 
    static int reduceMutual(List<List<BitSet>> cols, List<List<BitSet>> rows) {
        int countRemoved1 = reduce(cols, rows);
        if (countRemoved1 == -1)
            return -1;
 
        int countRemoved2 = reduce(rows, cols);
        if (countRemoved2 == -1)
            return -1;
 
        return countRemoved1 + countRemoved2;
    }
 
    static int reduce(List<List<BitSet>> a, List<List<BitSet>> b) {
        int countRemoved = 0;
 
        for (int i = 0; i < a.size(); i++) {
 
            BitSet commonOn = new BitSet();
            commonOn.set(0, b.size());
            BitSet commonOff = new BitSet();
 
            // determine which values all candidates of ai have in common
            for (BitSet candidate : a.get(i)) {
                commonOn.and(candidate);
                commonOff.or(candidate);
            }
 
            // remove from bj all candidates that don't share the forced values
            for (int j = 0; j < b.size(); j++) {
                final int fi = i, fj = j;
 
                if (b.get(j).removeIf(cnd -> (commonOn.get(fj) && !cnd.get(fi))
                        || (!commonOff.get(fj) && cnd.get(fi))))
                    countRemoved++;
 
                if (b.get(j).isEmpty())
                    return -1;
            }
        }
        return countRemoved;
    }
}