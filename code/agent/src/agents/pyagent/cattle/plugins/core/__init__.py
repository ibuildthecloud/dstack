import marshaller
import event_router
import event_handlers
from cattle.type_manager import register_type, MARSHALLER, ROUTER
from cattle.type_manager import POST_REQUEST_HANDLER

register_type(MARSHALLER, marshaller.Marshaller())
register_type(ROUTER, event_router.Router())
register_type(POST_REQUEST_HANDLER, event_handlers.PingHandler())
register_type(POST_REQUEST_HANDLER, event_handlers.ConfigUpdateHandler())
