package it.unibz.precise.hibernate.mysql.validation;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import it.unibz.precise.model.UniqueKeys;
import it.unibz.precise.model.validation.ExceptionDataExtractor;

/**
 * Assumes that Hibernate and MySQL are used and uses provider specific APIs
 * to determine the fields that caused a given DataAccessException corresponding
 * to a uniqueness constraint violation.
 * 
 * @author MatthiasP
 *
 */
@Service
public class MySQLUniqueFieldsExtractor implements ExceptionDataExtractor<DataAccessException, String[]> {
	
	/** Indicates whether the given error code represents a uniqueness constraint violation. */
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
	
	/** Returns the causing {@link ConstraintViolationException}, if available, or null otherwise. */
	private ConstraintViolationException toHibernateException(DataAccessException exc) {
		Throwable cause = exc.getCause();
		return cause instanceof ConstraintViolationException
			? (ConstraintViolationException)cause
			: null;
	}
	
	/** Returns the causing {@link MySQLIntegrityConstraintViolationException}, if available, or null otherwise. */
	private MySQLIntegrityConstraintViolationException toMySQLException(ConstraintViolationException exc) {
		SQLException sqlExc = exc.getSQLException();
		return sqlExc instanceof MySQLIntegrityConstraintViolationException
			? (MySQLIntegrityConstraintViolationException)sqlExc
			: null;
		
	}

	/** Returns the fields of a uniqueness constraint if the given exception represents a violation of such, null otherwise. */
	@Override
	public String[] apply(DataAccessException exc) {
		// Retrieve the causing Hibernate exception
		ConstraintViolationException hibernateExc = toHibernateException(exc);
		if (hibernateExc == null)
			return null;
		// Retrieve the causing MySQL exception
		MySQLIntegrityConstraintViolationException mySQLExc = toMySQLException(hibernateExc);
		if (mySQLExc == null || !isKnownMySQLUniquenessViolationError(mySQLExc.getErrorCode()))
			return null;
		// Lookup fields for the given constraint name.
		return UniqueKeys.lookup(hibernateExc.getConstraintName());
	}

}
