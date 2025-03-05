//set a fake api key
process.env.LLM_API_KEY = 'test-api-key';

const request = require('supertest');
const axios = require('axios');
const app = require('./llm-service'); 

afterAll(async () => {
    app.close();
  });

jest.mock('axios');

describe('LLM Service', () => {
  // Mock responses from external services
  axios.post.mockImplementation((url, data) => {
    if (url.startsWith('https://generativelanguage')) {
      return Promise.resolve({ data: { candidates: [{ content: { parts: [{ text: 'llmanswer' }] } }] } });
    } else if (url.startsWith('https://empathyai')) {
      return Promise.resolve({ data: { choices: [ {message: { content: 'llmanswer' } } ] } });
    }
  });

  // Test /ask endpoint
  it('the llm should reply', async () => {
    const response1 = await request(app)
      .post('/ask')
      .send({ question: 'a question', model: 'gemini' });

    const response2 = await request(app)
      .post('/ask')
      .send({ question: 'a question', model: 'empathy' });

    expect(response1.statusCode).toBe(200);
    expect(response1.body.answer).toBe('llmanswer');
    expect(response2.statusCode).toBe(200);
    expect(response2.body.answer).toBe('llmanswer');
  });

});