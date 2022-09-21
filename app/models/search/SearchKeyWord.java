package models.search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 关键字
 */
@Entity
@Table(name = "v1_search_key_word")
public class SearchKeyWord extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(name = "key_word")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String key;

    @Column(name = "sort")
    public int sort;

    public static Finder<Integer, SearchKeyWord> find = new Finder<>(SearchKeyWord.class);

    public void setId(int id) {
        this.id = id;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "SearchKeyWord{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", sort=" + sort +
                '}';
    }
}
