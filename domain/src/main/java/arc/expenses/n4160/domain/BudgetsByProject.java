package arc.expenses.n4160.domain;

import arc.athenarc.n4160.domain.*;

public class BudgetsByProject {

    private String id;
    private int year;
    private boolean fits;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isFits() {
        return fits;
    }

    public void setFits(boolean fits) {
        this.fits = fits;
    }
}
