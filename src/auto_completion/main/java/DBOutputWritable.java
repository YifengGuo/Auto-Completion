package auto_completion.main.java;

import org.apache.hadoop.mapred.lib.db.DBWritable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author yifengguo
 */
public class DBOutputWritable implements DBWritable {
    private String startint_phrase;
    private String following_word;
    private int count;

    public DBOutputWritable(String startint_phrase, String following_word, int count) {
        this.startint_phrase = startint_phrase;
        this.following_word = following_word;
        this.count = count;
    }

    @Override
    public void readFields(ResultSet arg0) throws SQLException {
        this.startint_phrase = arg0.getString(1);
        this.following_word = arg0.getString(2);
        this.count = arg0.getInt(3);
    }

    @Override
    public void write(PreparedStatement arg0) throws SQLException {
        arg0.setString(1, startint_phrase);
        arg0.setString(2, following_word);
        arg0.setInt(3, count);
    }
}
