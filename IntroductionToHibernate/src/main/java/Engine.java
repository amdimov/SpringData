import antlr.StringUtils;
import entities.Address;
import entities.Employee;
import entities.Project;
import entities.Town;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Engine implements Runnable{
    private final BufferedReader reader;
    private final EntityManager entityManager;

    public Engine(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void run() {
        try {
            System.out.print("Please choose the exercise: ");
            int answer = Integer.parseInt(reader.readLine());
            switch (answer){
                case 2 -> exerciseTwo();
                case 3 -> exerciseThree();
                case 4 -> exerciseFour();
                case 5 -> exerciseFive();
                case 6 -> exerciseSix();
                case 7 -> exerciseSeven();
                case 8 -> exerciseEight();
                case 9 -> exerciseNine();
                case 10 -> exerciseTen();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exerciseTwo() {
        Query query = entityManager.createQuery("UPDATE Town t SET t.name = UPPER(t.name) WHERE LENGTH(t.name) <= 5");
        entityManager.getTransaction().begin();
        System.out.println(query.executeUpdate() + " rows updated");
        entityManager.getTransaction().commit();
    }
    private void exerciseThree() throws IOException {
        System.out.print("Please enter employee's first and last name: ");
        try {
            String[] input = reader.readLine().split("\\s+");
            Long employee = entityManager.createQuery("SELECT count(e) FROM Employee e " +
                    "WHERE e.firstName = :fn AND e.lastName = :ln", Long.class)
                    .setParameter("fn", input[0])
                    .setParameter("ln", input[1])
                    .getSingleResult();
            System.out.println(employee == 0 ? "NO" : "YES");
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Incorrect name input!");
        }
    }
    private void exerciseFour() {
        entityManager.createQuery("SELECT e FROM Employee e WHERE e.salary > :salary", Employee.class)
                .setParameter("salary", BigDecimal.valueOf(50000L))
                .getResultStream()
                .map(Employee::getFirstName)
                .forEach(System.out::println);
    }
    private void exerciseFive() {
        entityManager.createQuery("SELECT e FROM Employee e WHERE e.department.name = :dName " +
                "ORDER BY e.salary, e.id", Employee.class)
        .setParameter("dName", "Research and Development")
        .getResultStream()
        .forEach(e -> System.out.printf("%s %s from %s - $%.2f%n",
                e.getFirstName(), e.getLastName(),
                e.getDepartment().getName(), e.getSalary()));
    }
    private void exerciseSix() throws IOException {
        System.out.print("Please enter last name of employee: ");
        String lastName = reader.readLine();

        Employee employee = entityManager.createQuery("SELECT e FROM Employee e WHERE e.lastName = :ln", Employee.class)
                .setParameter("ln", lastName)
                .getSingleResult();
        Address address = createNewAddress("Vitoshka 15", 32);
        entityManager.getTransaction().begin();
            employee.setAddress(address);
        entityManager.getTransaction().commit();
    }

    private Address createNewAddress(String text, int setTownId){
        Address address = new Address();
        entityManager.getTransaction().begin();
        address.setText(text);
        address.setTown(entityManager.find(Town.class, setTownId));
        entityManager.persist(address);
        entityManager.getTransaction().commit();
        return address;
    }
    private void exerciseSeven(){
        List<Address> resultList = entityManager.createQuery("SELECT a FROM Address a " +
                "ORDER BY a.employees.size DESC", Address.class)
                .setMaxResults(10)
                .getResultList();
        resultList.forEach(e ->
                System.out.printf("%s, %s - %d%n", e.getText(), e.getTown().getName(), e.getEmployees().size()));
    }
    private void exerciseEight() throws IOException {
        System.out.print("Please enter employee's id: ");
        int id = Integer.parseInt(reader.readLine());
        Employee employee = entityManager.find(Employee.class, id);
        if (employee == null){
            System.out.println("No such id");
            return;
        }
        System.out.printf("%s %s - %s%n", employee.getFirstName(), employee.getLastName(), employee.getJobTitle());

        entityManager.createQuery("SELECT p FROM Project p " +
                "ORDER BY p.name", Project.class)
                .getResultStream()
                .filter(e ->e.getEmployees().contains(employee))
                .forEach(e -> System.out.printf("\t  %s%n", e.getName()));
    }

    private void exerciseNine(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        entityManager.createQuery("SELECT p FROM Project p ORDER BY p.startDate DESC", Project.class)
                .setMaxResults(10)
                .getResultStream()
                .sorted(Comparator.comparing(Project::getName))
                .forEach(e ->
                                System.out.printf("Project name: %s%n\t    Project Description: %s%n\t    Project Start Date: " +
                                        "%s%n\t    Project End Date: %s%n", e.getName(), e.getDescription(),
                                        e.getStartDate().format(formatter),
                                        e.getEndDate() != null ? e.getEndDate().format(formatter) : null)
                        );
    }
    private void exerciseTen(){
        entityManager.createQuery("SELECT e FROM Employee e WHERE e.department.name IN(:eng, :td, :mark, :is)", Employee.class)
                .setParameter("eng", "Engineering")
                .setParameter("td", "Tool Design")
                .setParameter("mark", "Marketing")
                .setParameter("is", "Information Services")
                .getResultStream()
                .forEach(e -> System.out.printf("%s %s (%.2f)%n", e.getFirstName(), e.getLastName(), e.getSalary()));
            }
}

