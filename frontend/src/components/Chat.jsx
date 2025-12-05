import { useState, useEffect, useRef } from 'react';
import { 
  sendMessage, 
  getListingConversation, 
  markMessageAsRead
} from '../services/messagesService';
import { getListingById, updateListingStatus } from '../services/listingsService';
import { requestToBuy, confirmTransaction, rejectTransaction, getTransactionByListingId } from '../services/transactionService';
import Loader from './Loader';

const Chat = ({ listingId, recipientId, recipientName, listingTitle, listingPrice, onClose, currentUserId }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const [listing, setListing] = useState(null);
  const [transactionId, setTransactionId] = useState(null);
  const [transactionStatus, setTransactionStatus] = useState(null);
  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);
  
  // Check if there's a purchase request (from messages or transaction)
  const hasPurchaseRequest = transactionId && transactionStatus !== 'COMPLETED' && transactionStatus !== 'CANCELLED' || messages.some(msg => 
    msg.content?.includes('🛒 Purchase request')
  );
  
  // Check if transaction is confirmed (seller confirmed the sale)
  const isConfirmed = transactionStatus === 'COMPLETED' || messages.some(msg => 
    msg.content?.includes('✅ Sale confirmed') || msg.content?.includes('✅ Transaction confirmed')
  );
  
  // Check if request was rejected (seller declined)
  const isRejected = transactionStatus === 'CANCELLED' || messages.some(msg => 
    msg.content?.includes('❌ Purchase request has been declined') || msg.content?.includes('❌ Rejected')
  );

  // Load listing details to get seller info and price
  useEffect(() => {
    if (!listingId) return;

    const loadListing = async () => {
      try {
        const listingData = await getListingById(listingId);
        setListing(listingData);
      } catch (err) {
        console.error('Error loading listing:', err);
      }
    };

    loadListing();
  }, [listingId]);

  // Load existing transaction for this listing
  useEffect(() => {
    if (!listingId) return;

    const loadTransaction = async () => {
      try {
        // Try to get transaction for this listing
        const transaction = await getTransactionByListingId(listingId);
        if (transaction) {
          // Handle both single transaction object and array
          const tx = Array.isArray(transaction) ? transaction[0] : transaction;
          if (tx && tx.id) {
            setTransactionId(tx.id);
            setTransactionStatus(tx.status);
          }
        }
      } catch (err) {
        console.error('Error loading transaction:', err);
        // Transaction might not exist yet, which is fine
      }
    };

    loadTransaction();
  }, [listingId]);

  // Reload transaction when messages change (in case a new transaction was created)
  useEffect(() => {
    if (!listingId || !messages.length) return;
    
    // Check if there's a purchase request message but no transaction loaded
    const hasPurchaseMessage = messages.some(msg => 
      msg.content?.includes('🛒 Purchase request')
    );
    
    if (hasPurchaseMessage && !transactionId) {
      const loadTransaction = async () => {
        try {
          const transaction = await getTransactionByListingId(listingId);
          if (transaction) {
            const tx = Array.isArray(transaction) ? transaction[0] : transaction;
            if (tx && tx.id) {
              setTransactionId(tx.id);
              setTransactionStatus(tx.status);
            }
          }
        } catch (err) {
          console.error('Error reloading transaction:', err);
        }
      };
      
      loadTransaction();
    }
  }, [messages, listingId, transactionId]);
  
  // Determine if current user is the seller
  const isSeller = listing?.sellerId === currentUserId;

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
        
        // After loading messages, check if we need to load transaction
        // (transaction might have been created when buyer sent purchase request)
        if (messageList && messageList.some(msg => msg.content?.includes('🛒 Purchase request'))) {
          try {
            const transaction = await getTransactionByListingId(listingId);
            if (transaction) {
              const tx = Array.isArray(transaction) ? transaction[0] : transaction;
              if (tx && tx.id) {
                setTransactionId(tx.id);
                setTransactionStatus(tx.status);
              }
            }
          } catch (txErr) {
            console.error('Error loading transaction after messages:', txErr);
          }
        }
        
        // Don't automatically mark all as read on every load to prevent extra API calls
      } catch (err) {
        console.error('Error loading conversation:', err);
        setError(err.message || 'Failed to load conversation');
        setMessages([]);
      } finally {
        setLoading(false);
      }
    };

    loadConversation();
    
    // No automatic polling - messages will refresh when user sends a message or component remounts
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

  // Transaction handlers - call transaction APIs
  const handleRequestToBuy = async () => {
    if (!listingId || !currentUserId || sending) return;
    
    setSending(true);
    setError('');
    
    try {
      // Call transaction API: POST /api/transactions/request-to-buy?listingId={listingId}&buyerId={buyerId}
      const transaction = await requestToBuy(listingId, currentUserId);
      
      // Store transaction ID and status for later use (confirm/reject)
      if (transaction && transaction.id) {
        setTransactionId(transaction.id);
        setTransactionStatus(transaction.status || 'PENDING');
      }
      
      const price = listingPrice || listing?.price || 'this item';
      const priceText = typeof price === 'number' ? `$${parseFloat(price).toFixed(2)}` : price;
      
      // Send message to notify seller
      const sentMessage = await sendMessage({
        listingId: listingId,
        toUserId: recipientId,
        content: `🛒 Purchase request submitted for ${priceText}`,
      });
      
      // Add message to local state immediately
      setMessages(prev => [...prev, sentMessage]);
    } catch (err) {
      console.error('Error requesting to buy:', err);
      setError(err.message || 'Failed to submit purchase request');
    } finally {
      setSending(false);
    }
  };

  const handleConfirmTransaction = async () => {
    if (!listingId || sending) return;
    
    setSending(true);
    setError('');
    
    try {
      // Call API: PATCH /api/listings/{id}/status?status=SOLD
      await updateListingStatus(listingId, 'SOLD');
      
      // Send message to notify buyer
      const sentMessage = await sendMessage({
        listingId: listingId,
        toUserId: recipientId,
        content: `✅ Sale confirmed! Transaction approved.`,
      });
      
      // Add message to local state immediately
      setMessages(prev => [...prev, sentMessage]);
      
      // Update local state to reflect confirmed status
      setTransactionStatus('COMPLETED');
    } catch (err) {
      console.error('Error confirming sale:', err);
      setError(err.message || 'Failed to confirm sale');
      alert(err.message || 'Failed to confirm sale. Please try again.');
    } finally {
      setSending(false);
    }
  };

  const handleRejectTransaction = async () => {
    if (!listingId || sending) return;
    
    if (!window.confirm('Are you sure you want to reject this purchase request?')) {
      return;
    }
    
    setSending(true);
    setError('');
    
    try {
      // If transactionId is not set, try to load it first
      let txId = transactionId;
      if (!txId) {
        console.log('Transaction ID not found, attempting to load transaction for listing:', listingId);
        const transaction = await getTransactionByListingId(listingId);
        if (transaction) {
          // Handle both single transaction object and array
          const tx = Array.isArray(transaction) ? transaction[0] : transaction;
          if (tx && tx.id) {
            txId = tx.id;
            setTransactionId(txId);
            setTransactionStatus(tx.status);
            console.log('Transaction loaded:', tx);
          } else {
            throw new Error('Transaction not found. Please ensure the buyer has submitted a purchase request.');
          }
        } else {
          throw new Error('Transaction not found. Please ensure the buyer has submitted a purchase request.');
        }
      }
      
      if (!txId) {
        throw new Error('Transaction ID is required to reject the sale.');
      }
      
      console.log('Rejecting transaction with ID:', txId);
      
      // Call API: PATCH /api/transactions/{id}/status=CANCELLED
      await rejectTransaction(txId);
      setTransactionStatus('CANCELLED');
      
      // Send message to notify buyer
      const sentMessage = await sendMessage({
        listingId: listingId,
        toUserId: recipientId,
        content: `❌ Purchase request has been declined.`,
      });
      
      // Add message to local state immediately
      setMessages(prev => [...prev, sentMessage]);
    } catch (err) {
      console.error('Error rejecting transaction:', err);
      setError(err.message || 'Failed to reject transaction');
      alert(err.message || 'Failed to reject transaction. Please try again.');
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
      <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl h-[80vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="bg-indigo-600 text-white p-4 rounded-t-lg flex justify-between items-center flex-shrink-0">
          <div className="flex-1 min-w-0 pr-4">
            <h2 className="text-lg font-semibold truncate">{recipientName}</h2>
            {listingTitle && (
              <p className="text-sm text-indigo-200 truncate">{listingTitle}</p>
            )}
            {(listingPrice || listing?.price) && (
              <p className="text-sm text-indigo-100 font-medium mt-1">
                ${parseFloat(listingPrice || listing?.price || 0).toFixed(2)}
                {isConfirmed && (
                  <span className="ml-2 px-2 py-0.5 bg-indigo-700 rounded text-xs">
                    ✅ Confirmed
                  </span>
                )}
                {hasPurchaseRequest && !isConfirmed && !isRejected && (
                  <span className="ml-2 px-2 py-0.5 bg-indigo-700 rounded text-xs">
                    ⏳ Request Pending
                  </span>
                )}
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            className="text-white hover:text-gray-200 text-2xl font-bold flex-shrink-0"
          >
            ×
          </button>
        </div>

        {/* Messages */}
        <div 
          ref={messagesContainerRef}
          className="flex-1 min-h-0 overflow-y-auto p-4 bg-gray-50"
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
          <div className="px-4 py-2 bg-red-50 text-red-600 text-sm flex-shrink-0">
            {error}
          </div>
        )}

        {/* Transaction Actions */}
        {!loading && (listingPrice || listing) && (
          <div className="flex-shrink-0 px-4 py-3 bg-indigo-50 border-t border-indigo-200">
            {!isSeller ? (
              // Buyer actions
              <div className="flex flex-col gap-2">
                {!hasPurchaseRequest && !isConfirmed && !isRejected ? (
                  <button
                    onClick={handleRequestToBuy}
                    disabled={sending || isConfirmed}
                    className="w-full px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed font-medium transition-colors flex items-center justify-center gap-2"
                  >
                    {sending ? (
                      <>
                        <svg className="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Sending...
                      </>
                    ) : (
                      <>
                        🛒 Request to Buy
                        {(listingPrice || listing?.price) && ` ($${parseFloat(listingPrice || listing?.price || 0).toFixed(2)})`}
                      </>
                    )}
                  </button>
                ) : hasPurchaseRequest && !isConfirmed && !isRejected ? (
                  <div className="text-center py-2 px-4 bg-yellow-100 text-yellow-800 rounded-lg text-sm">
                    ⏳ Purchase request pending seller confirmation
                  </div>
                ) : isConfirmed ? (
                  <div className="text-center py-2 px-4 bg-green-100 text-green-800 rounded-lg text-sm">
                    ✅ Transaction confirmed! Sale approved.
                  </div>
                ) : isRejected ? (
                  <div className="text-center py-2 px-4 bg-gray-100 text-gray-800 rounded-lg text-sm">
                    ❌ Purchase request was declined
                  </div>
                ) : null}
              </div>
            ) : (
              // Seller actions
              <div className="flex flex-col gap-2">
                {hasPurchaseRequest && !isConfirmed && !isRejected ? (
                  <div className="flex gap-2">
                    <button
                      onClick={handleConfirmTransaction}
                      disabled={sending}
                      className="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed font-medium transition-colors"
                    >
                      {sending ? 'Sending...' : '✅ Confirm Sale'}
                    </button>
                    <button
                      onClick={handleRejectTransaction}
                      disabled={sending}
                      className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed font-medium transition-colors"
                    >
                      {sending ? 'Sending...' : '❌ Reject'}
                    </button>
                  </div>
                ) : isConfirmed ? (
                  <div className="text-center py-2 px-4 bg-green-100 text-green-800 rounded-lg text-sm">
                    ✅ Transaction confirmed! Sale approved.
                  </div>
                ) : !hasPurchaseRequest ? (
                  <div className="text-center py-2 px-4 bg-blue-100 text-blue-800 rounded-lg text-sm">
                    Waiting for purchase request from buyer
                  </div>
                ) : null}
              </div>
            )}
          </div>
        )}

        {/* Input */}
        <form onSubmit={handleSend} className="flex-shrink-0 p-4 bg-white border-t border-gray-200">
          <div className="flex gap-2">
            <input
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Type a message..."
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 text-gray-900 bg-white"
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

