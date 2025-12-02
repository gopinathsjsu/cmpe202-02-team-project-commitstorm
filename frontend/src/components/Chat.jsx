import { useState, useEffect, useRef } from 'react';
import { 
  sendMessage, 
  getListingConversation, 
  markMessageAsRead,
  markAllMessagesAsRead 
} from '../services/messagesService';
import Loader from './Loader';

const Chat = ({ listingId, recipientId, recipientName, listingTitle, onClose, currentUserId }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);

  // Load conversation
  useEffect(() => {
    if (!listingId || !currentUserId || !recipientId) return;

    const loadConversation = async () => {
      setLoading(true);
      setError('');
      try {
        // userId1 = sellerId (recipientId), userId2 = currentUserId (buyer)
        // API expects: /api/messages/conversation/listing/{listingId}/{userId1}/{userId2}
        const data = await getListingConversation(listingId, recipientId, currentUserId);
        // Handle both array and paginated response
        const messageList = Array.isArray(data) ? data : (data.content || data);
        setMessages(messageList || []);
        
        // Mark all messages as read
        if (messageList && messageList.length > 0) {
          await markAllMessagesAsRead(currentUserId);
        }
      } catch (err) {
        console.error('Error loading conversation:', err);
        setError(err.message || 'Failed to load conversation');
        setMessages([]);
      } finally {
        setLoading(false);
      }
    };

    loadConversation();
    
    // Poll for new messages every 3 seconds
    const interval = setInterval(loadConversation, 3000);
    return () => clearInterval(interval);
  }, [listingId, currentUserId, recipientId]);

  // Scroll to bottom when messages change
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!newMessage.trim() || sending) return;

    setSending(true);
    setError('');
    
    try {
      const messageData = {
        listingId: listingId,
        toUserId: recipientId,
        content: newMessage.trim(),
      };

      const sentMessage = await sendMessage(messageData);
      
      // Add message to local state immediately
      setMessages(prev => [...prev, sentMessage]);
      setNewMessage('');
      
      // Mark as read
      if (sentMessage.id) {
        await markMessageAsRead(sentMessage.id);
      }
    } catch (err) {
      console.error('Error sending message:', err);
      setError(err.message || 'Failed to send message');
    } finally {
      setSending(false);
    }
  };

  const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (minutes < 1440) return `${Math.floor(minutes / 60)}h ago`;
    return date.toLocaleDateString();
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl h-[80vh] flex flex-col">
        {/* Header */}
        <div className="bg-indigo-600 text-white p-4 rounded-t-lg flex justify-between items-center">
          <div>
            <h2 className="text-lg font-semibold">{recipientName}</h2>
            {listingTitle && (
              <p className="text-sm text-indigo-200">{listingTitle}</p>
            )}
          </div>
          <button
            onClick={onClose}
            className="text-white hover:text-gray-200 text-2xl font-bold"
          >
            ×
          </button>
        </div>

        {/* Messages */}
        <div 
          ref={messagesContainerRef}
          className="flex-1 overflow-y-auto p-4 bg-gray-50"
        >
          {loading ? (
            <Loader text="Loading messages..." />
          ) : error && messages.length === 0 ? (
            <div className="flex justify-center items-center h-full">
              <div className="text-red-500">{error}</div>
            </div>
          ) : messages.length === 0 ? (
            <div className="flex justify-center items-center h-full">
              <div className="text-gray-500">No messages yet. Start the conversation!</div>
            </div>
          ) : (
            <div className="space-y-3">
              {messages.map((msg) => {
                const isOwn = msg.senderId === currentUserId;
                return (
                  <div
                    key={msg.id || msg.messageId}
                    className={`flex ${isOwn ? 'justify-end' : 'justify-start'}`}
                  >
                    <div
                      className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                        isOwn
                          ? 'bg-indigo-600 text-white'
                          : 'bg-white text-gray-800 border border-gray-200'
                      }`}
                    >
                      <p className="text-sm">{msg.content}</p>
                      <p className={`text-xs mt-1 ${isOwn ? 'text-indigo-200' : 'text-gray-500'}`}>
                        {formatTime(msg.createdAt || msg.timestamp)}
                      </p>
                    </div>
                  </div>
                );
              })}
              <div ref={messagesEndRef} />
            </div>
          )}
        </div>

        {/* Error message */}
        {error && messages.length > 0 && (
          <div className="px-4 py-2 bg-red-50 text-red-600 text-sm">
            {error}
          </div>
        )}

        {/* Input */}
        <form onSubmit={handleSend} className="p-4 bg-white border-t border-gray-200">
          <div className="flex gap-2">
            <input
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Type a message..."
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              disabled={sending}
            />
            <button
              type="submit"
              disabled={sending || !newMessage.trim()}
              className="px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {sending ? 'Sending...' : 'Send'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Chat;

