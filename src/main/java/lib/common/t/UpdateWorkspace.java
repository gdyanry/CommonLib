package lib.common.t;

import lib.common.util.ConsoleUtil;
import lib.common.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class UpdateWorkspace {
    public static void main(String... args) throws IOException {
        updateWorkspace();
    }

    static void updateWorkspace() throws IOException {
        File dir = new File("d:/work/projects");
        File appDir = new File(dir, "HiWallet");
        Runtime runtime = Runtime.getRuntime();
        List<String> record = new LinkedList<>();
        Consumer<String> outputHandler = s -> {
            System.out.println(s);
            if (s.startsWith("BUILD ")) {
                record.add(String.format("%tT %s", System.currentTimeMillis(), s));
            }
        };
        for (File libDir : dir.listFiles()) {
            if (ifContinue(libDir.getAbsolutePath())) {
                ConsoleUtil.execCommand(runtime.exec("git pull", null, libDir), outputHandler);
                if (!libDir.equals(appDir)) {
                    File subDir = new File(libDir, libDir.getName().substring(6));
                    record.add(String.format("%n%tT %s", System.currentTimeMillis(), subDir.getName()));
                    runtime.exec("cmd /c start local_clean_build_mvn.bat", null, subDir);
//                    ConsoleUtil.execCommand(runtime.exec(new File(subDir, "local_clean_build_mvn.bat").getAbsolutePath()), outputHandler);
                }
            }
        }
        if (ifContinue(appDir.getAbsolutePath())) {
            record.add(String.format("%n%tT %s", System.currentTimeMillis(), appDir.getName()));
            runtime.exec("cmd /c start build_apk_with_multi_flavor_aar.bat", null, appDir);

            if (ifContinue("签名")) {
                String flavor = ConsoleUtil.readLine("flavor: ");
                String buildType = ConsoleUtil.readLine("build type: ");
                File desDir = new File("D:\\receivedFile\\HwSign");

                IOUtil.copyFile(new File(String.format("D:/work/projects/HiWallet/UserCenterBase/build/outputs/apk/%s/%s/UserCenterBase-%1$s-%2$s.apk", flavor, buildType)), new File(desDir, "Wallet.apk"));
                ConsoleUtil.readLine("请切换到内网：");
                runtime.exec("cmd /c start autowallet.bat", null, desDir);

                if (ifContinue("安装")) {
                    runtime.exec("cmd /c start start-adb.bat", null, desDir);
                }
            }
        }

        ConsoleUtil.debug("records>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        for (String s : record) {
            System.out.println(s);
        }
    }

    private static boolean ifContinue(String hint) {
        String input = ConsoleUtil.readLine(String.format("%s%n跳过请按n：", hint));
        return !input.equalsIgnoreCase("n");
//        System.out.println("------------------------------------------");
//        System.out.println(hint);
//        return true;
    }
}
