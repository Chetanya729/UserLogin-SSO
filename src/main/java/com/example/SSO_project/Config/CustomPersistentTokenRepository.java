package com.example.SSO_project.Config;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CustomPersistentTokenRepository implements PersistentTokenRepository {

    private JdbcTemplate jdbcTemplate;

    public CustomPersistentTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        jdbcTemplate.update("insert into persistent_logins(username,series,token,last_used) values (?,?,?,?)",token.getUsername(),token.getSeries(),token.getTokenValue(),token.getDate());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        jdbcTemplate.update("update persistent_logins set token=?, last_used=? where series=?", tokenValue,lastUsed,series);
    }

    @Override
    public @Nullable PersistentRememberMeToken getTokenForSeries(String seriesId) {
       try{
           return jdbcTemplate.queryForObject(
                   "select username, series, token, last_used from persistent_logins where series = ?",
                   (rs, rowNum) -> new PersistentRememberMeToken(
                           rs.getString("username"),
                           rs.getString("series"),
                           rs.getString("token"),
                           rs.getTimestamp("last_used")
                   ),
                   seriesId
           );
       }
       catch (Exception e){
           return null;
       }
    }

    @Override
    public void removeUserTokens(String username) {
        jdbcTemplate.update("delete from persistent_logins where username=?",username);
    }
}
