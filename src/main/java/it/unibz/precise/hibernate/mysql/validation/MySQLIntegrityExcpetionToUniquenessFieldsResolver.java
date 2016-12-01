package it.unibz.precise.hibernate.mysql.validation;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import it.unibz.precise.model.UniqueKeys;
import it.unibz.precise.model.validation.ExceptionResolver;

@Service
public class MySQLIntegrityExcpetionToUniquenessFieldsResolver
		implements ExceptionResolver<DataAccessException, String[]> {
	
	private boolean isKnownMySQLUniquenessViolationError(int errorCode) {
		switch (errorCode) {
		case 1022:	// ER_DUP_KEY
		case 1062:	// ER_DUP_ENTRY
		case 1169:	// ER_DUP_UNIQUE
		case 1557:	// ER_FOREIGN_DUPLICATE_KEY
		case 1586:	// ER_DUP_ENTRY_WITH_KEY_NAME
			return true;
		default:
			return false;
		}
	}
	
	private ConstraintViolationException toHibernateException(DataAccessException exc) {
		Throwable cause = exc.getCause();
		return cause instanceof ConstraintViolationException
			? (ConstraintViolationException)cause
			: null;
	}
	
	private MySQLIntegrityConstraintViolationException toMySQLException(ConstraintViolationException exc) {
		SQLException sqlExc = exc.getSQLException();
		return sqlExc instanceof MySQLIntegrityConstraintViolationException
			? (MySQLIntegrityConstraintViolationException)sqlExc
			: null;
		
	}

	@Override
	public String[] resolve(DataAccessException exc) {
		ConstraintViolationException hibernateExc = toHibernateException(exc);
		if (hibernateExc == null)
			return null;
		MySQLIntegrityConstraintViolationException mySQLExc = toMySQLException(hibernateExc);
		if (mySQLExc == null || !isKnownMySQLUniquenessViolationError(mySQLExc.getErrorCode()))
			return null;
		return UniqueKeys.lookup(hibernateExc.getConstraintName());
	}

}
