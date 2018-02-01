package cn.jcyh.peephole.control;

/**
 * Created by jogger on 2017/12/20.
 * 猫眼指令
 */

public class DoorbellCommand {
    private Command command;

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

   public static class Command {
        private String type;
        private String name;
        private String nums;

        @Override
        public String toString() {
            return "Command{" +
                    "type='" + type + '\'' +
                    ", name='" + name + '\'' +
                    ", nums='" + nums + '\'' +
                    '}';
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNums() {
            return nums;
        }

        public void setNums(String nums) {
            this.nums = nums;
        }
    }
}
