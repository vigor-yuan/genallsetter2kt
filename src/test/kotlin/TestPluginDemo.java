public class TestPluginDemo {

    private String name;
    private int age;
    private boolean isMale;
    private Long year;


    public static void main(String[] args) {
        TestPluginDemo test = new TestPluginDemo();
        TestPluginDemo test2 = TestPluginDemo.builder()
                .name(test.getName())
                .age(test.getAge())
                .isMale(test.isMale())
                .build();
    }

    public void testArgs(TestPluginDemo test) {

    }

    public static Builder builder() {
        return new Builder();
    }

    //builder模式
    public static class Builder {
        private String name;
        private int age;
        private boolean isMale;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder isMale(boolean isMale) {
            this.isMale = isMale;
            return this;
        }

        public TestPluginDemo build() {
            TestPluginDemo test = new TestPluginDemo();
            String name = test.getName();
            int age = test.getAge();
            boolean isMale = test.isMale();
            Long year = test.getYear();
            return test;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean male) {
        isMale = male;
    }

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }
}
