package ch.elexis.core.jpa.entities.listener;

import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.ExceptionHandler;
import org.eclipse.persistence.internal.databaseaccess.DatabaseCall;
import org.eclipse.persistence.queries.DatabaseQuery;

public class GlobalExceptionHandler implements ExceptionHandler{

	@Override
	public Object handleException(RuntimeException exception){
		if (exception instanceof DatabaseException)
		{
			DatabaseException databaseException = (DatabaseException) exception;
			DatabaseQuery databaseQuery = databaseException.getQuery();
			if (databaseException.getMessage().contains("Value too long for column"))
			{

				DatabaseCall databaseCall = databaseQuery.getCall();
				databaseQuery.getDescriptor().getAlias();
				databaseCall.getSQLString();
				System.out.println("LOG VALUES");
			}

			//StringUtils.abbreviate("", "", 6);
	
		}
		throw exception; // rethrow
	}
	
}
