import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    public static void main(String[] args) {
        String dirSaveGame = prepareDir();

        GameProgress gameProgress1 = new GameProgress(100, 1, 1, 10);
        GameProgress gameProgress2 = new GameProgress(200, 2, 2, 20);
        GameProgress gameProgress3 = new GameProgress(300, 3, 3, 30);

        saveProgress(gameProgress1, dirSaveGame + "//gp1.dat");
        saveProgress(gameProgress2, dirSaveGame + "//gp2.dat");
        saveProgress(gameProgress3, dirSaveGame + "//gp3.dat");

        zipFiles(dirSaveGame);

        deleteSaves(dirSaveGame);
    }

    private static String prepareDir() {
        //Получаем текущую папку, в которой запущенна программа (чтобы не было привязки к ОС)
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        //Создаем папку Games
        File mainDir = new File(System.getProperty("user.dir") + "//Games");
        if (mainDir.mkdir()) System.out.println("Каталог Games создан");
        else System.out.println("Каталог Games уже существует");

        return gamesDirFill(mainDir.getPath());
    }

    private static String gamesDirFill(String path) {
        File saveGamesDir = new File(path + "//savegames");
        if (saveGamesDir.mkdir()) System.out.println("Каталог savegames создан");
        else System.out.println("Каталог savegames уже существует");

        return saveGamesDir.getPath();
    }

    private static void saveProgress(GameProgress gameProgress, String path) {
        try (FileOutputStream fos = new FileOutputStream(path); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(gameProgress);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void zipFiles(String path) {
        //Получаем список всех файлов сохранений
        List<File> listSavedGames = getListSave(path);

        System.out.println("Список файлов для архивации: ");
        listSavedGames.forEach(System.out::println);

        //Открываем поток архива
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(path + "//saves.zip"))) {
            //На каждый файл открываем свой поток
            for (File save : listSavedGames) {
                try (FileInputStream fileInputStream = new FileInputStream(save.getPath())) {
                    //Создаем запись о файле в архиве
                    ZipEntry zipEntry = new ZipEntry(save.getName());
                    zipOutputStream.putNextEntry(zipEntry);

                    //Записываем файл в промежуточный буфер
                    byte[] buffer = new byte[fileInputStream.available()];
                    fileInputStream.read(buffer);

                    //Записываем буфер в архив
                    zipOutputStream.write(buffer);

                    //Закрываем запись файла в архив
                    zipOutputStream.closeEntry();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<File> getListSave(String path) {
        File dir = new File(path);
        List<File> listSavedGames = new ArrayList<>();

        if (dir.isDirectory()) {
            List<File> allFiles = Arrays.asList(dir.listFiles());

            //Фильтруем
            listSavedGames = allFiles.stream().filter(file -> file.getName().matches(".*\\.dat")).collect(Collectors.toList());
        }
        return listSavedGames;
    }

    private static void deleteSaves(String path) {
        //Получаем список всех файлов сохранений
        List<File> listSavedGames = getListSave(path);

        for (File save : listSavedGames){
            save.delete();
        }
    }
}
