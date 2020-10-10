package search;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static Scanner in = new Scanner(System.in);
    public static String[] people;
    public static Map<String, List<Integer>> index;

    public static void main(String[] args) {

        importData(args[1]);
//        importData("C:\\Users\\Mirek\\Documents\\hyperskill\\phone-book\\searchEngine.txt");
        index = buildInvertedIndex(people);
        displayMenu();
        selectOption();

        in.close();
        System.out.println("Bye!");


    }

    public static String[] importData(String pathToFile) {
        File file = new File(pathToFile);
        try(Scanner fromFile = new Scanner(file)){
            ArrayList<String> list = new ArrayList();
            while (fromFile.hasNext()) {
                list.add(fromFile.nextLine());
            }
            people = new String[list.size()];
            people = list.toArray(people);

        } catch(FileNotFoundException e) {
            System.out.println("File not found!");
        }
        return people;
    }

    public static void displayMenu(){
        System.out.println("\n=== Menu ===\n1. Find a person\n2. Print all people\n0. Exit\n");
    }

    public static void selectOption() {
        String option = in.nextLine();
        while (!option.equals("0")) {
            switch(option) {
                case "1":
                    System.out.println("Select a matching strategy: ALL, ANY, NONE");
                    String strategy = in.nextLine();
                    System.out.printf("\nEnter a name or email to search all suitable people.%n");
                    String[] target = in.nextLine().split(" ");
                    String result = findPeople(people, target, strategy);
                    System.out.println(result.isEmpty() ? "No matching people found." : result.toString());
                    break;
                case "2":
                    printAllPeople(people);
                    break;
                default:
                    System.out.println("Incorrect option! Try again");
            }
            displayMenu();
            option = in.nextLine();
        }
    }

/*    public static String findPeople(String[] people, String target){
        StringBuilder sb = new StringBuilder();
        boolean isFound = false;
        for (int i = 0; i < people.length; i++) {
            if (people[i].toLowerCase().contains(target.toLowerCase())) {
                sb.append(people[i] + "\n");
                isFound = true;
            }
        }

        if(isFound) {
            sb.setLength(sb.length() - 1);
            return sb.toString();
        } else {
            return "No matching people found.";
        }
    }*/
/*    public static String findPeople(String[] people, String target) {
        StringBuilder sb = new StringBuilder();
        Map<Integer, String> map = convertArrayToMap(people);
        target = target.toLowerCase();
        if (index.containsKey(target)) { // ensuring user input is lowercase
            sb.append(index.get(target).size() + " persons found:\n");
            for (Integer i : index.get(target)) {
                sb.append(map.get(i) + "\n");
            }
        } else {
            return "No matching people found.";
        }

        return sb.toString();
    }*/

    public static String findPeople(String[] people, String[] target, String strategy) {
        Map<Integer, String> map = convertArrayToMap(people);
        String result = "";
        switch (strategy) {
            case "ANY" :
                result = findAny(map, target, strategy);
                break;
            case "ALL" :
                result = findAll(map, target, strategy);
                break;
            case "NONE" :
                result = findNone(map, target, strategy);
                break;
            default :
                result = "UNKNOWN STRATEGY";
        }

        return result;
    }

    public static String findAny(Map<Integer, String> map, String[] target, String strategy) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < target.length; i++) {
            String word = target[i].toLowerCase();
            if (index.containsKey(word)) { // ensuring user input is lowercase
                for (Integer num : index.get(word)) {
                    sb.append(map.get(num) + "\n");
                }
            }
        }
        return sb.toString();
    }

    public static String findAll(Map<Integer, String> map, String[] target, String strategy) {
        StringBuilder sb = new StringBuilder();
        List<Integer> indicesForFirstWord = new ArrayList<>();
        List<Integer> indicesForSecondWord = new ArrayList<>();
        List<Integer> indicesForThirdWord = new ArrayList<>();
        Pattern pattern = Pattern.compile("^(\\w+) (\\w+) (\\w+)?$");
        Matcher matcher;
        for (int i = 0; i < target.length; i++) {
            matcher = pattern.matcher(target[i]);
            if (matcher.matches()) {
                if (matcher.group(1) != null) {
                    indicesForFirstWord = index.get(target[0].toLowerCase());
                }
                if (matcher.group(2) != null) {
                    indicesForSecondWord = index.get(target[1].toLowerCase());
                }
                if (matcher.group(3) != null) {
                    indicesForThirdWord = index.get(target[2].toLowerCase());
                }
            }
        }

        indicesForFirstWord.retainAll(indicesForSecondWord);
        indicesForFirstWord.retainAll(indicesForThirdWord);
        for (int i = 0; i < indicesForFirstWord.size(); i++) {
            sb.append(map.get(indicesForFirstWord.get(i)) + "\n");
        }

        return sb.toString();
    }

    public static String findNone(Map<Integer, String> map, String[] target, String strategy) {
        StringBuilder sb = new StringBuilder();
        Map<Integer, String> mapCopy = new HashMap<>(map);
        for (int i = 0; i < target.length; i++) {
            String word = target[i].toLowerCase();
            if (index.containsKey(word)) { // ensuring user input is lowercase
                List<Integer> nums = index.get(word);
                for (Integer num : nums) {
                    for (Map.Entry<Integer, String> e : map.entrySet()) {
                        if (e.getKey().equals(num)) {
                            mapCopy.remove(e.getKey());
                        }
                    }
                }
            }
        }
        for (Map.Entry<Integer, String> e : mapCopy.entrySet()) {
            sb.append(e.getValue() + "\n");
        }
        return sb.toString();
    }

    public static void printAllPeople(String[] people) {
        System.out.println("=== List of people ===");
        for(String person : people){
            System.out.println(person);
        }
    }

    public static Map<String, List<Integer>> buildInvertedIndex(String[] array) {
//        String firstname, lastname, email;
        Map<String, List<Integer>> map = new HashMap<>();
//        Scanner scanner = new Scanner(System.in);
        Pattern pattern = Pattern.compile("^(\\w+) (\\w+)(\\s?\\w+@\\w+.com)?$");
        Matcher matcher;
        for (int i = 0; i < array.length; i++) {
            matcher = pattern.matcher(array[i]);
            if (matcher.matches()) {
                int numOfGroups;
                if (matcher.group(3) != null) {
                    numOfGroups = 3;
                } else {
                    numOfGroups = 2;
                }
                for (int j = 1; j <= numOfGroups; j++) {
                    String key = matcher.group(j).toLowerCase().trim(); // making keys lowercase
                    if (map.containsKey(key)) {
                        map.get(key).add(i);
                    } else {
                        map.put(key, new ArrayList<>());
                        map.get(key).add(i);
                    }
                }
            }

        }
//        map.forEach((key, value) -> System.out.println(key + " : " + value));
        return map;
    }

    public static Map<Integer, String> convertArrayToMap(String[] array) {
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < array.length; i++) {
            map.put(i, array[i]);
        }
        return map;
    }
}
