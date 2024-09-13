import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


interface AccountService {
    void withdraw(String accountNum, BigDecimal amountToWithdraw);
    void deposit(String accountNum, BigDecimal amountToDeposit);
    BigDecimal calculateInterest(String accountNum);
    void createAccount(String accountType, String accountNum, BigDecimal initialBalance); 
}


abstract class Account {
    protected String accountNum;
    protected BigDecimal balance;

    public Account(String accountNum, BigDecimal initialBalance) {
        this.accountNum = accountNum;
        this.balance = initialBalance;
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(amount);
            System.out.println("Deposit successful! New balance: " + formatCurrency(balance));
        } else {
            System.out.println("Deposit amount must be greater than zero.");
        }
    }

    public BigDecimal getBalance() {
        return balance;
    }

    
    public abstract void withdraw(BigDecimal amount);
    public abstract BigDecimal calculateInterest();

    protected String formatCurrency(BigDecimal amount) {
        return String.format("R%.2f", amount);
    }
}


class SavingsAccount extends Account {
    static final BigDecimal MINIMUM_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.03"); 

    public SavingsAccount(String accountNum, BigDecimal initialBalance) {
        super(accountNum, initialBalance);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Withdrawal amount must be greater than zero.");
            return;
        }
        BigDecimal newBalance = balance.subtract(amount);
        if (newBalance.compareTo(MINIMUM_BALANCE) >= 0) {
            balance = newBalance;
            System.out.println("Withdrawal successful! New balance: " + formatCurrency(balance));
        } else {
            System.out.println("Withdrawal failed! Minimum balance of R1000 must be maintained.");
        }
    }

    @Override
    public BigDecimal calculateInterest() {
        return balance.multiply(INTEREST_RATE);
    }
}


class CurrentAccount extends Account {
    private BigDecimal overdraftLimit;
    private static final BigDecimal MAX_OVERDRAFT_LIMIT = new BigDecimal("100000.00");

    public CurrentAccount(String accountNum, BigDecimal initialBalance, BigDecimal overdraftLimit) {
        super(accountNum, initialBalance);
        this.overdraftLimit = overdraftLimit.min(MAX_OVERDRAFT_LIMIT); 
    }

    @Override
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Withdrawal amount must be greater than zero.");
            return;
        }
        BigDecimal availableToWithdraw = balance.add(overdraftLimit);
        if (amount.compareTo(availableToWithdraw) <= 0) {
            balance = balance.subtract(amount);
            System.out.println("Withdrawal successful! New balance: " + formatCurrency(balance));
        } else {
            System.out.println("Withdrawal failed! Exceeds overdraft limit.");
        }
    }

    @Override
    public BigDecimal calculateInterest() {
        
        return BigDecimal.ZERO;
    }
}


class AccountServiceImpl implements AccountService {
    private Map<String, Account> accountMap = new HashMap<>();

    public AccountServiceImpl() {
        
        accountMap.put("SAV123", new SavingsAccount("SAV123", new BigDecimal("5000.00")));
        accountMap.put("CUR456", new CurrentAccount("CUR456", new BigDecimal("1000.00"), new BigDecimal("25000.00")));
    }

    @Override
    public void withdraw(String accountNum, BigDecimal amountToWithdraw) {
        Account account = accountMap.get(accountNum);
        if (account != null) {
            account.withdraw(amountToWithdraw);
        } else {
            System.out.println("Account not found.");
        }
    }

    @Override
    public void deposit(String accountNum, BigDecimal amountToDeposit) {
        Account account = accountMap.get(accountNum);
        if (account != null) {
            account.deposit(amountToDeposit);
        } else {
            System.out.println("Account not found.");
        }
    }

    @Override
    public BigDecimal calculateInterest(String accountNum) {
        Account account = accountMap.get(accountNum);
        if (account != null) {
            return account.calculateInterest();
        } else {
            System.out.println("Account not found.");
            return BigDecimal.ZERO;
        }
    }

    @Override
    public void createAccount(String accountType, String accountNum, BigDecimal initialBalance) {
        if (accountMap.containsKey(accountNum)) {
            System.out.println("Account number already exists. Please choose a different number.");
            return;
        }

        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Initial balance must be greater than or equal to zero.");
            return;
        }

        Account newAccount;
        if (accountType.equalsIgnoreCase("SAVINGS")) {
            if (initialBalance.compareTo(SavingsAccount.MINIMUM_BALANCE) < 0) {
                System.out.println("Initial balance for savings account must be at least R1000.");
                return;
            }
            newAccount = new SavingsAccount(accountNum, initialBalance);
        } else if (accountType.equalsIgnoreCase("CURRENT")) {
            newAccount = new CurrentAccount(accountNum, initialBalance, new BigDecimal("25000.00")); 
        } else {
            System.out.println("Invalid account type. Please choose 'SAVINGS' or 'CURRENT'.");
            return;
        }
        
        accountMap.put(accountNum, newAccount);
        System.out.println("Account created successfully: Account Number - " + accountNum);
    }
}


public class App {
    public static void main(String[] args) {
        AccountService accountService = new AccountServiceImpl();
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n--- SETHMAG BANK SYSTEM ---");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Calculate Interest");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");

            int choice = getIntInput(scanner);

            switch (choice) {
                case 1:
                    System.out.print("Enter account type (SAVINGS/CURRENT): ");
                    String accountType = scanner.nextLine();
                    System.out.print("Enter account number: ");
                    String accountNum = scanner.nextLine();
                    System.out.print("Enter initial balance: ");
                    BigDecimal initialBalance = getBigDecimalInput(scanner);
                    accountService.createAccount(accountType, accountNum, initialBalance);
                    break;
                case 2:
                    System.out.print("Enter account number: ");
                    String depositAccount = scanner.nextLine();
                    System.out.print("Enter amount to deposit: ");
                    BigDecimal depositAmount = getBigDecimalInput(scanner);
                    accountService.deposit(depositAccount, depositAmount);
                    break;
                case 3:
                    System.out.print("Enter account number: ");
                    String withdrawAccount = scanner.nextLine();
                    System.out.print("Enter amount to withdraw: ");
                    BigDecimal withdrawAmount = getBigDecimalInput(scanner);
                    accountService.withdraw(withdrawAccount, withdrawAmount);
                    break;
                case 4:
                    System.out.print("Enter account number: ");
                    String interestAccount = scanner.nextLine();
                    BigDecimal interest = accountService.calculateInterest(interestAccount);
                    System.out.println("Interest earned: " + interest);
                    break;
                case 5:
                    exit = true;
                    System.out.println("Thank you for using Sethmag Bank System!");
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
                    break;
            }
        }
        scanner.close();
    }

    private static int getIntInput(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input, please enter a number: ");
            }
        }
    }

    private static BigDecimal getBigDecimalInput(Scanner scanner) {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input, please enter a valid amount: ");
            }
        }
    }
}
